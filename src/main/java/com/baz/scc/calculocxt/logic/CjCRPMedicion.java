package com.baz.scc.calculocxt.logic;

import com.baz.scc.commons.util.CjCRUtils;
import com.baz.scc.calculocxt.dao.CjCRPMedicionDao;
import com.baz.scc.calculocxt.support.CjCRPAppConfig;
import com.baz.scc.commons.util.CjCRParseUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bean para la configuración de CxT.
 * <br><br>Copyright 2013 Banco Azteca. Todos los derechos reservados.
 *
 * @author B938469 Israel G.M.
 */
@Component("applicationT")
public class CjCRPMedicion {

    @Autowired
    private CjCRPAppConfig appConfig;
    
    @Autowired
    private CjCRPMedicionDao transferencia;
    
    private static final Logger log = Logger.getLogger(CjCRPMedicion.class);
    private static final String USUARIO = "PRTxC";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    
    private Integer procesoBloque;
    public String procesoModo;
    public Integer procesoFecha;
    public Integer procesoIDPais;
    public Integer procesoDiasRetraso;    
    public boolean procesoTransferencia;
    public boolean procesoCxT;
    long begin = System.currentTimeMillis();
    long end = System.currentTimeMillis();

    public void procesar() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        Date fechaBase;
        Date fechaFin;
        int fecha;
        
        //Obtener la configucación
        obtenerConfiguracion();
        
        calendar.setTime(dateFormat.parse(Integer.toString(procesoFecha)));
            
        fechaBase = calendar.getTime();
        
        fechaFin = getFechaRango();
            
        if(appConfig.isProcesoFechaRango() && !fechaBase.before(fechaFin)) {
            throw new IllegalStateException("La fecha de fin no es mayor a la fecha de inicio");
        }
        
        while(fechaBase.before(fechaFin)) {
            fecha = CjCRParseUtils.toInteger(dateFormat.format(fechaBase));
            
            log.info(String.format("--------------- Realizando procesamiento para fecha [%d]", fecha));
            
            faseTransferencia(fecha);
            faseCalculoCxT(fecha);
            
            calendar.add(Calendar.DATE, 1);
                
            fechaBase = calendar.getTime();
        }
    }
    
    //Fase de transferecia
    private void faseTransferencia(int fecha) {
        List listaRegistros;

        // Verificar si el proceso de transferencia se encuentra activo
        if (procesoTransferencia) {
            
            //Limpieza de tablas
            begin = System.currentTimeMillis();
            LimparAcumula(fecha);
            end = System.currentTimeMillis();
            log.info(CjCRUtils.concat("----- Limpieza de tablas [", CjCRUtils.formatElapsedTime(begin, end), "]"));

            //Obtener mediciones
            begin = System.currentTimeMillis();
            listaRegistros = obtenerRegistros(fecha);
            end = System.currentTimeMillis();
            log.info(CjCRUtils.concat("----- Obtencion de mediciones [", CjCRUtils.formatElapsedTime(begin, end), "]"));

            //Transferencia de mediciones
            begin = System.currentTimeMillis();
            if(!listaRegistros.isEmpty()){
            registarAcumula(listaRegistros);
            end = System.currentTimeMillis();
            log.info(CjCRUtils.concat("----- Transferencia de mediciones [", CjCRUtils.formatElapsedTime(begin, end), "]"));
            }        
        }
    }

    // Fase de Calculo CxT
    private void faseCalculoCxT(int fechaProceso) {
        if (procesoCxT) {
            try {
                //Realizar el calculo de CxT            
                begin = System.currentTimeMillis();

                log.info(String.format("Inicia proceso de calculo de CxT - fecha: %d", fechaProceso));

                transferencia.calcularCxTBd(fechaProceso, procesoIDPais, USUARIO);

                end = System.currentTimeMillis();

                log.info(String.format("Proceso de calculo de CxT finalizado - fecha: %d [%s]", 
                        fechaProceso, CjCRUtils.formatElapsedTime(begin, end)));
            } catch (Exception ex) {
                log.error("CxT: Error Excepcion en el proceso de Cálculo de CxT" + ex);
            }
        }
    }
    
    // Fase de transferencia: Paso 1 Limpieza de tablas
    private void LimparAcumula(int fechaProceso) {

        // Paso 1 Limpieza de tablas
        try {
            log.info("Inicia Proceso de limpieza con Fecha=" + fechaProceso + " y Pais=" + procesoIDPais);
            transferencia.limpiarAcumulaBD(fechaProceso, procesoIDPais);
            log.info("Finaliza Proceso de limpieza");
        } catch (Exception ex) {
            log.error("Limpieza: Error Excepcion:  " + ex);
        }
        
    }

    private List obtenerRegistros(int fechaProceso) {

        List listaMediciones = new ArrayList();

        // Paso 2 Filtrado y Obtencion de Mediciones 
        try {
            log.info("Inicia Proceso de Filtrado y Obtencion de Mediciones con Fecha=" + fechaProceso + " y Pais=" + procesoIDPais);
            listaMediciones = transferencia.getMediciones(fechaProceso, procesoIDPais);
            if (listaMediciones.isEmpty()) {
                log.info("No existen mediciones  con Fecha=" + fechaProceso + " y Pais=" + procesoIDPais);
            } else {
                log.info("Se obtuvieron " + listaMediciones.size() + " Mediciones");
            }

        } catch (Exception ex) {
            log.error("Error Excepcion en la Obtencion de mediciones" + ex);
        }
        return listaMediciones;
    }

    // Fase de transferencia: Paso 2 Filtrado y Obtencion de Mediciones 
    private void registarAcumula(List listaRegistros) {

        // Dividir la lista obtenida en bloques de tamaño dado por DIMENSIONBLOQUE
        List listaBloque = new ArrayList();
        int cont = 0;
        int bloque = 0;
        
        if (!listaRegistros.isEmpty()) {
            
            log.info("Inicia Proceso registro de Mediciones en BDOracle en TACJCXTACUMULA, por bloques de " + procesoBloque + " Registros");
            
            for (int i = 0; i < listaRegistros.size(); i++) {                
                listaBloque.add(listaRegistros.get(i));
                cont++;
                if (cont == procesoBloque || i == listaRegistros.size() - 1) {
                    try {
                        transferencia.registrarMedicionesBd(listaBloque, USUARIO);
                        listaBloque.clear();
                        bloque++;
                        log.info("Bloque " + bloque + " Registrado");
                        cont = 0;
                    } catch (Exception ex) {
                        log.error("Registrar: Error Excepcion en el Registro de mediciones" + ex);
                    }
                }
            }
        } else {
            log.info("Lista de mediciones vacia");
        }
    }

    //Obtencion de Modo Procesamiento: Pais, Fecha
    private void obtenerConfiguracion() {

        //Obtener el modo de procesamiento
        procesoModo = appConfig.getProcesoModo();
        procesoTransferencia = appConfig.isProcesoTransferencia();
        procesoCxT = appConfig.isProcesoCxT();
        procesoBloque = appConfig.getProcesoBloque();
        procesoDiasRetraso =  appConfig.getProcesoDiasRetraso();

        if (procesoModo.equalsIgnoreCase("automatico")) {
            
            //El rango se desactiva
            appConfig.setProcesoFechaRango(false);

            // En modo automatico obtener la fecha actual menos ( d días de atraso)
            Calendar calendario = GregorianCalendar.getInstance();
            calendario.add(Calendar.DATE, -procesoDiasRetraso); // Restar d Dias al calendario
            Date fecha = calendario.getTime();      // Fecha - d
            SimpleDateFormat formato = new SimpleDateFormat("yyyyMMdd"); // Formato Año Mes Dia
            procesoFecha = Integer.valueOf(formato.format(fecha));// Conversion atipo requerido Entero
            

        } else {// Si es Reproceso

            // Obtener la fecha del archivo properties
            procesoFecha = appConfig.getProcesoFecha();
        }

        //Si el ID del pais es null el traslado se realzará para todos los paises else obtener Id;
        procesoIDPais = appConfig.getProcesoIDPais();
    }
    
    private Date getFechaRango() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        Date fecha;
        
        if(!appConfig.isProcesoFechaRango()) {
            fecha = dateFormat.parse(Integer.toString(procesoFecha));
        } else {
            fecha = dateFormat.parse(Integer.toString( appConfig.getProcesoFechaFin()));
        }
        
        calendar.setTime(fecha);
        calendar.add(Calendar.DATE, 1);
        
        return calendar.getTime();
    }
}
