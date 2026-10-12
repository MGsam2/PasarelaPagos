package com.pasarelapagos.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(GraalVMRuntimeHints.IsoRuntimeHintsRegistrar.class)
public class GraalVMRuntimeHints {

    static class IsoRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. Registro de recursos de configuración XML para jPOS y j8583
            hints.resources().registerPattern("iso8583/*.xml");
            hints.resources().registerPattern("*.xml");

            // 2. Registro de reflexión para clases dinámicas de jPOS
            try {
                hints.reflection().registerType(
                        Class.forName("org.jpos.iso.packager.GenericPackager"),
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS
                );
                hints.reflection().registerType(
                        Class.forName("org.jpos.iso.ISOMsg"),
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS
                );
            } catch (ClassNotFoundException e) {
                // Ignorar si no está presente en el classpath durante AOT
            }
        }
    }
}