/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baz.scc.calculocxt.support;

/**
 *
 * @author B938469
 */
public class CjCRPAppConfig {

    private String procesoModo;
    private Integer procesoDiasRetraso;
    private Integer procesoFecha;
    private Integer procesoFechaFin;
    private Integer procesoIDPais;
    private Integer procesoBloque;
    private boolean procesoTransferencia;
    private boolean procesoFechaRango;
    private boolean procesoCxT;

    public Integer getProcesoDiasRetraso() {
        return procesoDiasRetraso;
    }

    public void setProcesoDiasRetraso(Integer procesoDiasRetraso) {
        this.procesoDiasRetraso = procesoDiasRetraso;
    }

    public boolean isProcesoFechaRango() {
        return procesoFechaRango;
    }

    public void setProcesoFechaRango(boolean procesoFechaRango) {
        this.procesoFechaRango = procesoFechaRango;
    }

    public boolean isProcesoTransferencia() {
        return procesoTransferencia;
    }

    public void setProcesoTransferencia(boolean procesoTransferencia) {
        this.procesoTransferencia = procesoTransferencia;
    }

    public boolean isProcesoCxT() {
        return procesoCxT;
    }

    public void setProcesoCxT(boolean procesoCxT) {
        this.procesoCxT = procesoCxT;
    }

    public Integer getProcesoBloque() {
        return procesoBloque;
    }

    public void setProcesoBloque(Integer procesoBloque) {
        this.procesoBloque = procesoBloque;
    }

    public String getProcesoModo() {
        return procesoModo;
    }

    public void setProcesoModo(String procesoModo) {
        this.procesoModo = procesoModo;
    }

    public Integer getProcesoFecha() {
        return procesoFecha;
    }

    public void setProcesoFecha(Integer procesoFecha) {
        this.procesoFecha = procesoFecha;
    }

    public Integer getProcesoFechaFin() {
        return procesoFechaFin;
    }

    public void setProcesoFechaFin(Integer procesoFechaFin) {
        this.procesoFechaFin = procesoFechaFin;
    }

    public Integer getProcesoIDPais() {
        return procesoIDPais;
    }

    public void setProcesoIDPais(Integer procesoIDPais) {
        this.procesoIDPais = procesoIDPais;
    }
}
