package com.pasarelapagos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Configuración de compilación nativa AOT (GraalVM Native Image 25).
 * Registra recursos XML de mapeo ISO y reflexión para las clases dinámicas de jPOS y j8583.
 */
@Configuration
@ImportRuntimeHints(GraalVMRuntimeHints.IsoRuntimeHintsRegistrar.class)
public class GraalVMRuntimeHints {

    private static final Logger log = LoggerFactory.getLogger(GraalVMRuntimeHints.class);

    static class IsoRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. Registro de plantillas y recursos XML
            hints.resources().registerPattern("iso8583/*.xml");
            hints.resources().registerPattern("*.xml");

            // 2. Registro de clases dinámicas cargadas por reflexión en tiempo de compilación AOT
            String[] classesToRegister = {
                    "org.jpos.iso.packager.GenericPackager",
                    "org.jpos.iso.ISOMsg",
                    "org.jpos.iso.IFA_NUMERIC",
                    "org.jpos.iso.IFA_BITMAP",
                    "org.jpos.iso.IFA_LLNUM",
                    "org.jpos.iso.IF_CHAR",
                    "org.jpos.iso.IFA_LLCHAR",
                    "org.jpos.iso.ISOBitMap",
                    "org.jpos.iso.ISOField",
                    "com.solab.iso8583.IsoMessage",
                    "com.solab.iso8583.MessageFactory"
            };

            for (String className : classesToRegister) {
                try {
                    Class<?> clazz = Class.forName(className);
                    hints.reflection().registerType(
                            clazz,
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_METHODS
                    );
                } catch (ClassNotFoundException e) {
                    log.debug("AOT Hints: Clase opcional {} no presente en el classpath", className);
                }
            }
        }
    }
}
