package com.prikolz.justhelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JustClassLoader extends ClassLoader {

    private final String classPath;

    public JustClassLoader(String classPath, ClassLoader parent) {
        super(parent);
        this.classPath = classPath;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return getParent().loadClass(name);
        } catch (ClassNotFoundException e) {
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(Paths.get(classPath, name.replace(".", File.separator) + ".class"));
            } catch (Exception ex) {
                throw new ClassNotFoundException();
            }
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(classPath, name.replace(".", File.separator) + ".class"));
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            return super.findClass(name);
        }
    }
}