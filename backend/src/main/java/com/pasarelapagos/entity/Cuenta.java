package com.pasarelapagos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
    private String numeroCuenta;

    @Column(nullable = false, unique = true, length = 19)
    private String pan;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

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