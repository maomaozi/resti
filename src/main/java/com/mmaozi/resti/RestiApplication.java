package com.mmaozi.resti;

import com.mmaozi.di.ContainedApp;
import com.mmaozi.di.annotations.Main;
import com.mmaozi.di.utils.ReflectionUtils;
import com.mmaozi.resti.exception.RestiBaseException;
import com.mmaozi.resti.resource.ResourceDispatcher;
import com.mmaozi.resti.server.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class RestiApplication {

    private final ContainedApp containedApp = new ContainedApp(RestiApplication.class);

    @Main
    public void bootstrap(Class<?> userAppClass, Object... args) throws Exception {
        List<Class<?>> userClasses = scanPackages(userAppClass);

        userClasses.forEach(containedApp::register);
        containedApp.getInstance(ResourceDispatcher.class).build(userClasses);
        containedApp.getInstance(HttpServer.class).run();
    }

    public void run(Class<?> userAppClass, Object... args) {
        log.info("run");
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
