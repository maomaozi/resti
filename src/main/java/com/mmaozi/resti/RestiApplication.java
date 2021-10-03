package com.mmaozi.resti;

import com.mmaozi.di.ContainedApp;
import com.mmaozi.di.annotations.Main;
import com.mmaozi.di.utils.ReflectionUtils;
import com.mmaozi.resti.exception.RestiBaseException;
import com.mmaozi.resti.resource.ResourceDispatcher;
import com.mmaozi.resti.server.HttpServer;

import java.util.List;
import java.util.stream.Collectors;

public class RestiApplication {

    private final ContainedApp containedApp = new ContainedApp(RestiApplication.class);

    @Main
    public void main(Class<?> userAppClass, Object... args) throws Exception {
        List<Class<?>> userClasses = scanPackages(userAppClass);

        userClasses.forEach(containedApp::register);
        containedApp.getInstance(ResourceDispatcher.class).build(userClasses);
        containedApp.getInstance(HttpServer.class).run();
    }

    public void run(Class<?> userAppClass, Object... args) {
        containedApp.addSingleton(this.getClass(), this);
        containedApp.run(userAppClass, args);
    }


    private List<Class<?>> scanPackages(Class<?> clazz) {
        return ReflectionUtils.findAllClassesInPackage(clazz.getPackage()).stream()
                              .map((String className) -> {
                                  try {
                                      return Class.forName(className);
                                  } catch (ClassNotFoundException ex) {
                                      throw new RestiBaseException("");
                                  }
                              })
                              .collect(Collectors.toList());
    }
}
