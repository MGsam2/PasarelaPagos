package com.pasarelapagos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity//mapea la clase java directo con la tabla en psql 
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//PK autoincrement
    private Long id;
//fetchtypelazy evita traer los datos del cliente a la BD hasta que explicitamente  se llame a cuenta.getcliente. optimiza memory y reduce consultas sql
    @ManyToOne(fetch = FetchType.LAZY, optional = false) //muchas cuentas a un cliente
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
    private String numeroCuenta;

        //contiene los 16 a 19 digitos de la tarjeta, unique garantiza por restriccion en BD no exista 2 tarjeatas duplicadas
    @Column(nullable = false, unique = true, length = 19)
    private String pan;

//aplicara loscampos de tipo bigdecimal dice a jpa que cree columa tipo numeric(12, 5) soporta 13 int y 2 decimal, para no perder centavos
    @Column(nullable = false, precision = 15, scale = 2) 
    private BigDecimal saldo;// dinero disponible de la cuenta en el core bancario

    @Column(nullable = false, length = 20)
    private String estado;

    public Cuenta() {
    }

    public Cuenta(
            Cliente cliente,
            String numeroCuenta,
            String pan,
            BigDecimal saldo,
            String estado) {

        this.cliente = cliente;
        this.numeroCuenta = numeroCuenta;
        this.pan = pan;
        this.saldo = saldo;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}