package com.pasarelapagos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity//mapea la clase java directo con la tabla en psql 
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//PK autoincrement
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)//muchas transacciona a una cuenta
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String estado;

    //almacena el resuptado enviado del core bancario 00 aprobado, 51 sin fondos, 14 tarjeta invalida
    @Column(name = "codigo_respuesta", length = 2)
    private String codigoRespuesta;

    //guarda el numeo de auditoria de la solicitud enviada por el terminal tcp/api para quedar reg en el historial pasarela
    @Column(length = 6)
    private String stan;

    //almacena tiempo exacto en que ocurrio el intento de pago para conciliacion bancaria
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public Transaccion() {
    }

    public Transaccion(
            Cuenta cuenta,
            BigDecimal monto,
            String tipo,
            String estado,
            String codigoRespuesta,
            String stan,
            LocalDateTime fechaHora) {

        this.cuenta = cuenta;
        this.monto = monto;
        this.tipo = tipo;
        this.estado = estado;
        this.codigoRespuesta = codigoRespuesta;
        this.stan = stan;
        this.fechaHora = fechaHora;
    }

    public Long getId() {
        return id;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodigoRespuesta() {
        return codigoRespuesta;
    }

    public void setCodigoRespuesta(String codigoRespuesta) {
        this.codigoRespuesta = codigoRespuesta;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}