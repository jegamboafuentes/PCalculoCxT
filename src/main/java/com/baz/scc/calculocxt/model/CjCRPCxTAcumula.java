package com.baz.scc.calculocxt.model;

/**
 * Bean para la configuración de CxT.
 * <br><br>Copyright 2013 Banco Azteca. Todos los derechos reservados.
 *
 * @author B938469 Israel G.M.
 */
public class CjCRPCxTAcumula {

    private Integer sucursalId;
    private Integer fechaId;
    private Integer origenId;
    private Integer productoId;
    private Integer conceptoId;
    private Integer totalOperacionId;

    public Integer getTotalOperacionId() {
        return totalOperacionId;
    }

    public void setTotalOperacionId(Integer totalOperacionId) {
        this.totalOperacionId = totalOperacionId;
    }
    
    private float tiempoTotal;
    private Integer calculo;
    private String fechaCalculo;
    private float tipoCobro;
    private Integer unidadId;

    @Override
    public String toString() {
        return "CjCRPCxTAcumula{" + "sucursalId=" + sucursalId + ", fechaId=" + fechaId + ", origenId=" + origenId + ", productoId=" + productoId + ", conceptoId=" + conceptoId + ", tipoOperacionId=" + totalOperacionId + '}';
    }

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
    }

    public Integer getFechaId() {
        return fechaId;
    }

    public void setFechaId(Integer fechaId) {
        this.fechaId = fechaId;
    }

    public Integer getOrigenId() {
        return origenId;
    }

    public void setOrigenId(Integer origenId) {
        this.origenId = origenId;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public Integer getConceptoId() {
        return conceptoId;
    }

    public void setConceptoId(Integer conceptoId) {
        this.conceptoId = conceptoId;
    }
    
    public float getTiempoTotal() {
        return tiempoTotal;
    }

    public void setTiempoTotal(float tiempoTotal) {
        this.tiempoTotal = tiempoTotal;
    }

    public Integer getCalculo() {
        return calculo;
    }

    public void setCalculo(Integer calculo) {
        this.calculo = calculo;
    }

    public String getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(String fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }

    public float getTipoCobro() {
        return tipoCobro;
    }

    public void setTipoCobro(float tipoCobro) {
        this.tipoCobro = tipoCobro;
    }

    public Integer getUnidadId() {
        return unidadId;
    }

    public void setUnidadId(Integer unidadId) {
        this.unidadId = unidadId;
    }
}
