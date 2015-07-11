package com.baz.scc.calculocxt.dao;

import com.baz.scc.calculocxt.model.CjCRPCxTAcumula;
import com.baz.scc.commons.dao.CjCRPaisDao;
import com.baz.scc.commons.model.CjCROracleResponse;
import com.baz.scc.commons.model.CjCRPais;
import com.baz.scc.commons.support.CjCRDaoConfig;
import com.baz.scc.commons.util.CjCRDaoUtils;
import com.baz.scc.commons.util.CjCRDaoUtils.ListStructureArray;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Bean para la configuración de CxT.
 * <br><br>Copyright 2013 Banco Azteca. Todos los derechos reservados.
 *
 * @author B938469 Israel G.M.
 */
@Repository
public class CjCRPMedicionDao {

    private static final Logger log = Logger.getLogger(CjCRPMedicionDao.class);
    
    @Autowired
    @Qualifier("as400JdbcTemplate")
    private JdbcTemplate as400JdbcTemplate;
    @Autowired
    @Qualifier("usrcajaJdbcTemplate")
    private JdbcTemplate usrcajaJdbcTemplate;
    CjCRPCxTAcumula acumulaBean = new CjCRPCxTAcumula();
    private static final ListStructureArray<CjCRPCxTAcumula> listStructureArray;
    private static final String TYPCJCXT0002_DESCRIPTOR = "%s.TYPCJCXT0002";
    @Autowired
    private CjCRPaisDao paisDao;
    
    @Autowired
    private CjCRDaoConfig daoConfig;
    
    private Map<Integer, CjCRPais> maPaises;
    CjCRPais objPais = new CjCRPais();

    public CjCROracleResponse limpiarAcumulaBD(final Integer fecha, final Integer idPais) {
        return usrcajaJdbcTemplate.execute(getLimpiarAcumulastatement(),
                new CallableStatementCallback<CjCROracleResponse>() {
            @Override
            public CjCROracleResponse doInCallableStatement(CallableStatement cs)
                    throws SQLException, DataAccessException {

                CjCROracleResponse or = new CjCROracleResponse();

                CjCRDaoUtils.addIntNotNull(cs, 1, fecha);
                CjCRDaoUtils.addInt(cs, 2, idPais);//Pais especifico o null (null=todos los paises)                
                cs.registerOutParameter(3, OracleTypes.NUMBER);
                cs.registerOutParameter(4, OracleTypes.VARCHAR);

                cs.execute();

                or.setStatus(cs.getInt(3));
                or.setMsg(cs.getString(4));

                if (or.getStatus() == 0) {
                    log.info("Proceso de Limpieza realizado Correctamente ");
                } else {
                    log.info("El Proceso de Limpieza no se ha realizado Correctamente ");
                }
                return or;

            }
        });
    }

    public List<CjCRPCxTAcumula> getMediciones(int fecha, Integer Idpais) {
        
        List<CjCRPCxTAcumula> listaRegistros = new ArrayList<CjCRPCxTAcumula>();
        //int mediciones =0;
        String sql;
        
        
        
        log.info("Construccion de la consulta");        

        if (Idpais != null) {//Si el Idpais es diferente de nulo se obtienen las mediciones del pais especificado
            StringBuilder statementPais = new StringBuilder();
            objPais = paisDao.getPais(Idpais);

            statementPais.append("select FISUCURSAL, FIFECHA, FIORIGEN, FIPRODUCTO, FISUBPRODUCTO, FITOTOPERS, FNTIEMPOTOTAL from ");
            statementPais.append(objPais.getBibBanco());
            statementPais.append(".TACJCFACUMULADOCXT where FIPRODUCTO>0 AND FISUBPRODUCTO<>-200  AND FIFECHA=");
            statementPais.append(fecha);

            sql = statementPais.toString();

            log.info("Envìo de Consulta:" + sql);
            listaRegistros = as400JdbcTemplate.query(sql, new MedicionesMapper(), (Object[]) null);
          

        } else if (Idpais == null) { //Si el Idpais es nulo se obtienen las mediciones de todos los paises
            maPaises = paisDao.getPaises();
            
            Iterator<CjCRPais> it = maPaises.values().iterator();

            while (it.hasNext()) {
                
                StringBuilder statementPaises = new StringBuilder();
                List<CjCRPCxTAcumula> temp = new ArrayList<CjCRPCxTAcumula>();

                CjCRPais pais = it.next();
                statementPaises.append("SELECT FISUCURSAL, FIFECHA, FIORIGEN, FIPRODUCTO, FISUBPRODUCTO, FITOTOPERS, FNTIEMPOTOTAL from ");
                statementPaises.append(pais.getBibBanco());
                statementPaises.append(".TACJCFACUMULADOCXT where FIPRODUCTO>0 AND FISUBPRODUCTO<>-200 AND FIFECHA=");
                statementPaises.append(fecha);
                sql = statementPaises.toString();

                log.info("Envìo de Consulta: " + sql);
                temp=as400JdbcTemplate.query(sql, new MedicionesMapper(), (Object[]) null);
                
                //mediciones = listaRegistros.size();
                log.info("Mediciones Obtenidas de AS400: " + temp.size());
                
                listaRegistros.addAll(temp);
                
                //mediciones= listaRegistros.size()- mediciones;
                log.info("Total de mediciones agregadas a lista: " + listaRegistros.size());
                
            }
        }        
        return listaRegistros;
    }

    class MedicionesMapper implements RowMapper<CjCRPCxTAcumula> {

        @Override
        public CjCRPCxTAcumula mapRow(ResultSet rs, int i) throws SQLException {

            CjCRPCxTAcumula mediciones = new CjCRPCxTAcumula();

            mediciones.setSucursalId(rs.getInt(1));
            mediciones.setFechaId(rs.getInt(2));
            mediciones.setOrigenId(rs.getInt(3));
            mediciones.setProductoId(rs.getInt(4));
            mediciones.setConceptoId(rs.getInt(5));
            mediciones.setTotalOperacionId(rs.getInt(6));
            mediciones.setTiempoTotal(rs.getFloat(7));
            return mediciones;
        }
    }

    static {
        listStructureArray = new ListStructureArray<CjCRPCxTAcumula>() {
            @Override
            public Object getObject(CjCRPCxTAcumula mediciones) { 
                Object[] row = new Object[7];

                row[0] = mediciones.getSucursalId();
                row[1] = mediciones.getFechaId();
                row[2] = mediciones.getOrigenId();
                row[3] = mediciones.getProductoId();
                row[4] = mediciones.getConceptoId();
                row[5] = mediciones.getTotalOperacionId();
                row[6] = mediciones.getTiempoTotal();

                return row;
            }
        };
    }

    public CjCROracleResponse registrarMedicionesBd(final List<CjCRPCxTAcumula> listamediciones,
            final String usuario) {

        return usrcajaJdbcTemplate.execute(getRegistrarMedicionesStatement(),
                new CallableStatementCallback<CjCROracleResponse>() {
            @Override
            public CjCROracleResponse doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
                {
                    CjCROracleResponse or = new CjCROracleResponse();
                    ResultSet rs;
                
                    cs.registerOutParameter(1, OracleTypes.CURSOR);
                    CjCRDaoUtils.addArrayNotNull(cs, 2, daoConfig.getSentence(TYPCJCXT0002_DESCRIPTOR), 
                            listStructureArray.getArray(listamediciones));
                    CjCRDaoUtils.addString(cs, 3, usuario);
                    cs.registerOutParameter(4, OracleTypes.NUMBER);
                    cs.registerOutParameter(5, OracleTypes.VARCHAR);

                    cs.execute();

                    or.setStatus(cs.getInt(4));
                    or.setMsg(cs.getString(5));
                    rs = (ResultSet) cs.getObject(1);

                    if (or.getStatus() == 0)
                        log.info("Registro de mediciones realizado correctamente OK");
                    else
                        log.info("Registro de mediciones Incorrecto");
                    while (rs.next())
                        log.info("Registro - Error : " + rs.getString(1));
                    
                    return or;
                }
            }
        });
    }

    public CjCROracleResponse calcularCxTBd(final Integer fecha, final Integer idPais,
            final String usuario) {
        return usrcajaJdbcTemplate.execute(getCalcularCxTStatement(),
                new CallableStatementCallback<CjCROracleResponse>() {
            @Override
            public CjCROracleResponse doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {

                CjCROracleResponse or = new CjCROracleResponse();
                ResultSet rs;

                cs.registerOutParameter(1, OracleTypes.CURSOR);
                CjCRDaoUtils.addIntNotNull(cs, 2, fecha);
                CjCRDaoUtils.addInt(cs, 3, idPais);
                CjCRDaoUtils.addString(cs, 4, usuario);
                cs.registerOutParameter(5, OracleTypes.NUMBER);
                cs.registerOutParameter(6, OracleTypes.VARCHAR);

                cs.execute();

                or.setStatus(cs.getInt(5));
                or.setMsg(cs.getString(6));
                rs = (ResultSet) cs.getObject(1);

                if (or.getStatus() == 0) 
                    log.info("Calculo de CxT realizado correctamente");
                else
                    log.info("Calculo de CxT no se realizò correctamente");         
                while (rs.next())
                    log.info("Registro - Error : " + rs.getString(1));
                
                return or;

            }
        });
    }

    public String getLimpiarAcumulastatement() {
        return daoConfig.getSentence("call %s.PQCJCXT0001.PACJCXTLD0001(?,?,?,?)");
    }

    public String getRegistrarMedicionesStatement() {
        return daoConfig.getSentence("{? = call %s.PQCJCXT0001.FNCJCXTLU0001 (?,?,?,?)");
    }

    public String getCalcularCxTStatement() {
        return daoConfig.getSentence("{? = call %s.PQCJCXT0001.FNCJCXTLU0002(?,?,?,?,?)}");
    }
}