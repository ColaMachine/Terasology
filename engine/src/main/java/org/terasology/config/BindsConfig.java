/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.config;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.BindAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.BindableAxis;
import org.terasology.input.BindableButton;
import org.terasology.input.DefaultBinding;
import org.terasology.input.Input;
import org.terasology.input.InputSystem;
import org.terasology.input.RegisterBindAxis;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.events.ButtonEvent;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.module.predicates.FromModule;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User binds configuration. This holds the key/mouse binding for Button Binds. They are sorted by package.
 *
 * @author Immortius
 */
public final class BindsConfig {
    private static final Logger logger = LoggerFactory.getLogger(BindsConfig.class);

    private ListMultimap<SimpleUri, Input> data = ArrayListMultimap.create();//一个simpleuri 对应一个输入符号

    public BindsConfig() {
    }

    /**
     * Sets this BindsConfig to be identical to other
     *
     * @param other The BindsConfig to copy
     */
    public void setBinds(BindsConfig other) { //复制其他的配置
        data.clear();//清空
        data.putAll(other.data);//重新填充
    }

    public Collection<Input> getBinds(SimpleUri uri) {
        return data.get(uri);//根据uri获取绑定的键位
    }

    /**
     * Returns whether an input bind has been registered with the BindsConfig.
     * It may just have trivial None input.
     *
     * @param uri The bind's uri
     * @return Whether the given bind has been registered with the BindsConfig
     */
    public boolean hasBinds(SimpleUri uri) {
        return !data.get(uri).isEmpty();
    }//简单的uri

    /**
     * Sets the inputs for a given bind, replacing any previous inputs
     *
     * @param bindUri
     * @param inputs
     */
    public void setBinds(SimpleUri bindUri, Input... inputs) {//这个binduri到底是个什么东西呢
        setBinds(bindUri, Arrays.asList(inputs));//engine:attack这样的
    }

    public void setBinds(SimpleUri bindUri, Iterable<Input> inputs) {
        Set<Input> uniqueInputs = Sets.newLinkedHashSet(inputs);

        // Clear existing usages of the given inputs//如果案件已经被绑定了 就删除原来的绑定
        Iterator<Input> iterator = data.values().iterator();//
        while (iterator.hasNext()) {
            Input i = iterator.next();
            if (uniqueInputs.contains(i)) {
                iterator.remove();
            }
        }
        data.replaceValues(bindUri, uniqueInputs);//一对多 的关系 就变成 n条key value
    }

    /**
     * @return A new BindsConfig, with inputs set from the DefaultBinding annotations on bind classes
     */
    public static BindsConfig createDefault() {//
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        BindsConfig config = new BindsConfig();
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {//遍历手游模块的id
            if (moduleManager.getRegistry().getLatestModuleVersion(moduleId).isCodeModule()) {
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                        config.addDefaultsFor(moduleId, environment.getTypesAnnotatedWith(RegisterBindButton.class, new FromModule(environment, moduleId)));
                    }
                }
            }
        }
        return config;
    }

    /**
     * Updates a config with any binds that it may be missing, through reflection over RegisterBindButton annotations
     */
    public void updateForChangedMods() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            if (moduleManager.getRegistry().getLatestModuleVersion(moduleId).isCodeModule()) {
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                        updateInputsFor(moduleId, environment.getTypesAnnotatedWith(RegisterBindButton.class, new FromModule(environment, moduleId)));
                    }
                }
            }
        }
    }

    private void updateInputsFor(Name moduleId, Iterable<Class<?>> classes) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
                SimpleUri bindUri = new SimpleUri(moduleId, info.id());
                if (!hasBinds(bindUri)) {
                    addBind(moduleId, buttonEvent, info);
                }
            }
        }
    }

    private void addDefaultsFor(Name moduleId, Iterable<Class<?>> classes) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
                addBind(moduleId, buttonEvent, info);
            }
        }
    }

    private void addBind(Name moduleName, Class<?> buttonEvent, RegisterBindButton info) {
        List<Input> defaultInputs = Lists.newArrayList();
        for (Annotation annotation : buttonEvent.getAnnotations()) {
            if (annotation instanceof DefaultBinding) {
                DefaultBinding defaultBinding = (DefaultBinding) annotation;
                Input input = defaultBinding.type().getInput(defaultBinding.id());
                if (!data.values().contains(input)) {
                    defaultInputs.add(input);
                }
            }
        }
        SimpleUri bindUri = new SimpleUri(moduleName, info.id());
        setBinds(bindUri, defaultInputs.toArray(new Input[defaultInputs.size()]));
    }

    public void applyBinds(InputSystem inputSystem, ModuleManager moduleManager) {
        inputSystem.clearBinds();
        registerButtonBinds(inputSystem, moduleManager.getEnvironment(), moduleManager.getEnvironment().getTypesAnnotatedWith(RegisterBindButton.class));
        registerAxisBinds(inputSystem, moduleManager.getEnvironment(), moduleManager.getEnvironment().getTypesAnnotatedWith(RegisterBindAxis.class));
    }

    private void registerAxisBinds(InputSystem inputSystem, ModuleEnvironment environment, Iterable<Class<?>> classes) {
        for (Class registerBindClass : classes) {
            RegisterBindAxis info = (RegisterBindAxis) registerBindClass.getAnnotation(RegisterBindAxis.class);
            Name moduleId = environment.getModuleProviding(registerBindClass);
            SimpleUri id = new SimpleUri(moduleId, info.id());
            if (BindAxisEvent.class.isAssignableFrom(registerBindClass)) {
                BindableButton positiveButton = inputSystem.getBindButton(new SimpleUri(info.positiveButton()));
                BindableButton negativeButton = inputSystem.getBindButton(new SimpleUri(info.negativeButton()));
                if (positiveButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing positive button \"{}\"", id, info.positiveButton());
                    continue;
                }
                if (negativeButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing negative button \"{}\"", id, info.negativeButton());
                    continue;
                }
                try {
                    BindableAxis bindAxis = inputSystem.registerBindAxis(id.toString(), (BindAxisEvent) registerBindClass.newInstance(), positiveButton, negativeButton);
                    bindAxis.setSendEventMode(info.eventMode());
                    logger.debug("Registered axis bind: {}", id);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register axis bind \"{}\"", id, e);
                }
            } else {
                logger.error("Failed to register axis bind \"{}\", does not extend BindAxisEvent", id);
            }
        }
    }

    private void registerButtonBinds(InputSystem inputSystem, ModuleEnvironment environment, Iterable<Class<?>> classes) {
        for (Class registerBindClass : classes) {//通过注解找到的class
            RegisterBindButton info = (RegisterBindButton) registerBindClass.getAnnotation(RegisterBindButton.class);
            SimpleUri bindUri = new SimpleUri(environment.getModuleProviding(registerBindClass), info.id());
            if (BindButtonEvent.class.isAssignableFrom(registerBindClass)) {
                try {
                    BindableButton bindButton = inputSystem.registerBindButton(bindUri, info.description(), (BindButtonEvent) registerBindClass.newInstance());
                    bindButton.setMode(info.mode());
                    bindButton.setRepeating(info.repeating());

                    for (Input input : getBinds(bindUri)) {
                        if (input != null) {
                            inputSystem.linkBindButtonToInput(input, bindUri);
                        }
                    }

                    logger.debug("Registered button bind: {}", bindUri);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register button bind \"{}\"", e);
                }
            } else {
                logger.error("Failed to register button bind \"{}\", does not extend BindButtonEvent", bindUri);
            }
        }
    }

    static class Handler implements JsonSerializer<BindsConfig>, JsonDeserializer<BindsConfig> {

        @Override
        public BindsConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BindsConfig result = new BindsConfig();
            JsonObject inputObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : inputObj.entrySet()) {
                SetMultimap<String, Input> map = context.deserialize(entry.getValue(), SetMultimap.class);
                for (String id : map.keySet()) {
                    SimpleUri uri = new SimpleUri(new Name(entry.getKey()), id);
                    result.data.putAll(uri, map.get(id));
                }
            }
            return result;
        }

        @Override
        public JsonElement serialize(BindsConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            SetMultimap<Name, SimpleUri> bindByModule = HashMultimap.create();
            for (SimpleUri key : src.data.keySet()) {
                bindByModule.put(key.getModuleName(), key);
            }
            List<Name> sortedModules = Lists.newArrayList(bindByModule.keySet());
            Collections.sort(sortedModules);
            for (Name moduleId : sortedModules) {
                SetMultimap<String, Input> moduleBinds = HashMultimap.create();
                for (SimpleUri bindUri : bindByModule.get(moduleId)) {
                    moduleBinds.putAll(bindUri.getObjectName().toString(), src.data.get(bindUri));
                }
                JsonElement map = context.serialize(moduleBinds, SetMultimap.class);
                result.add(moduleId.toString(), map);
            }
            return result;
        }
    }

}
