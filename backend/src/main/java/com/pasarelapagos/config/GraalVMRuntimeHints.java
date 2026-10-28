package com.pasarelapagos.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/*CONFIGURACIÓN DE COMPILACIÓN NATIVA (GraalVM):
 Le indica al compilador Ahead-of-Time (AOT) de Spring qué archivos y clases dinámicas 
 debe conservar al crear el archivo binario nativo, evitando que las elimine por error.*/
@Configuration
@ImportRuntimeHints(GraalVMRuntimeHints.IsoRuntimeHintsRegistrar.class)
public class GraalVMRuntimeHints {

    /*REGISTRADOR DE PISTAS (HINTS) PARA ISO 8583:
     Clase estática que implementa la interfaz de Spring para inyectar configuraciones
     manuales al motor de GraalVM durante el proceso de empaquetado.*/
    static class IsoRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

            // 1. Registro de recursos estáticos (Configuraciones):
            // Evita que el compilador borre los archivos XML donde configuramos el formato de los mensajes ISO
            hints.resources().registerPattern("iso8583/*.xml");
            hints.resources().registerPattern("*.xml");

            // 2. Registro de reflexión (Reflection):
            // Fuerza a GraalVM a incluir las clases base de jPOS (GenericPackager e ISOMsg)
            // permitiendo que el sistema las instancie dinámicamente en tiempo de ejecución
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
/*"El GraalVMRuntimeHints resuelve la incompatibilidad entre librerías financieras tradicionales (jPOS) y 
la compilación moderna AOT (Ahead-of-Time). Al registrar explícitamente los XMLs y habilitar la reflexión 
para el GenericPackager, permite compilar la pasarela en una imagen nativa ultra rápida y de bajo consumo de RAM."*/