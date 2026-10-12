package com.pasarelapagos.ruteador;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RuteadorTest {

    @Autowired
    private Ruteador ruteador;

    @Test
    void debeEnviarPan5222ABancoB() {

        String pan = "5222222222222222";

        String banco =
                ruteador.determinarBanco(pan);

        assertEquals(
                "BANCO_B",
                banco
        );
    }
}