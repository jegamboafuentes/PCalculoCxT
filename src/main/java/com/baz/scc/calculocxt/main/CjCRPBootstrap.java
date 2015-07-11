package com.baz.scc.calculocxt.main;

import com.baz.scc.calculocxt.logic.CjCRPMedicion;
import com.baz.scc.commons.util.CjCRSpringContext;
import org.apache.log4j.Logger;

/**
 * <br><br>Copyright 2013 Banco Azteca. Todos los derechos reservados.
 *
 * @author B938469 Israel G.M.
 */
public class CjCRPBootstrap {

    private static final Logger log = Logger.getLogger(CjCRPBootstrap.class);

    public static void main(String[] args) {
        try {
            CjCRSpringContext.init();

            CjCRPMedicion application = CjCRSpringContext.getBean(CjCRPMedicion.class);
            
            application.procesar();
        } catch (Exception ex) {
            log.error(String.format("Error en aplicaci\u00F3n - ", ex.getMessage()), ex);

        }
    }
}
