package com.baz.scc.calculocxt.test;

import com.baz.scc.calculocxt.dao.CjCRPMedicionDao;
import com.baz.scc.calculocxt.model.CjCRPCxTAcumula;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author B938469
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class CjCRPTransferenciaTest {

    @Autowired
    @Qualifier("usrcajaJdbcTemplate")
    private JdbcTemplate usrcajaJdbcTemplate;
    @Autowired
    private CjCRPMedicionDao transDao;
    private static final String USUARIO = "PRTxCTest";
    private static final Integer IDPAIS= 1;
    public Integer fecha;
    
    public boolean procesoTransferencia;
    public boolean procesoCxT;

    @Before
    public void testStart() {
        String sql = "DELETE FROM TACJCXTACUMULA WHERE FIFECHAID IN (19990101,18880101) ";
        usrcajaJdbcTemplate.execute(sql);
    }

    @Test
    public void registrarAcumulaTest() {
        //Flujo: Insertar - validar la insercion

        List<CjCRPCxTAcumula> registroTest = new ArrayList<CjCRPCxTAcumula>();
        List<CjCRPCxTAcumula> registroActual = new ArrayList<CjCRPCxTAcumula>();

        //Registro de prueba: IDPais=1 Sucursal=519  fechaId= 19990101 origen=1 producto=32 concepto=0 tipooperacion=5 tiempo=5000

        //Se agrega a la lista el Registro de Prueba 
        registroTest.add(configuracionTest("registro"));


        //Insertar Registro de prueba en BD Orcl
        transDao.registrarMedicionesBd(registroTest, USUARIO);

        //Obtener el registro insertado
        registroActual = obtenerRegistroTest(fecha, IDPAIS);

        //La prueba falla cuando el registro obtenido es diferente del registro insertado
        //Assert.assertEquals(registroTest, registroActual);

        Assert.assertTrue(registroTest.toString().equals(registroActual.toString()));

    }

    @Test
    public void limpiezaTest() {
        /*Limpieza Test Flujo: Limpiar registro de prueba - validar limpieza*/

        int numRegistros;

        //Registro de prueba2: IDPais=1 Sucursal=333 fechaId=15550101 origen=1 producto=33 concepto=0 tipooperacion=6 tiempo=408       
        usrcajaJdbcTemplate.execute(registrolLimpiezaTest( configuracionTest("limpieza")));

        //Limpar registro con pais y fecha de prueba
        transDao.limpiarAcumulaBD(fecha, IDPAIS);
        //Realizar consulta de los registros       
        numRegistros = contarRegistroTest(fecha, IDPAIS);
        //contar registros existentes para el pais y fecha de prueba
        Assert.assertTrue(numRegistros == 0);

    }

    @After
    public void testEnd() {
        String sql = "DELETE FROM TACJCXTACUMULA WHERE FIFECHAID IN (19990101,18880101) ";
        usrcajaJdbcTemplate.execute(sql);

    }

    private int contarRegistroTest(Integer fecha, Integer pais) {
//
//        String sql = "SELECT COUNT (ROWID) FROM TACJCXTACUMULA INNER JOIN TACJGEOSUCURSAL"
//                + " ON TACJCXTACUMULA.FISUCURSALID=TACJGEOSUCURSAL.FISUCURSALID"
//                + " AND TACJCXTACUMULA.FIFECHAID=" + fecha
//                + " AND TACJGEOSUCURSAL.FIPAISID=" + pais;


        String sql = "SELECT COUNT (ROWID) FROM TACJCXTACUMULA WHERE TACJCXTACUMULA.FIFECHAID=" + fecha;

        return contarAcumulaOrcl(sql);
    }

    private int contarAcumulaOrcl(String sql) {
        Object conteo;
        conteo = usrcajaJdbcTemplate.queryForObject(sql, new CountMapper());
        return parsearInteger(conteo);
    }

    class CountMapper implements RowMapper<Object> {

        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            Object count;
            count = (rs.getInt(1));
            return count;
        }
    }

    private List<CjCRPCxTAcumula> obtenerRegistroTest(Integer fecha, Integer pais) {
        
            String sql = "SELECT FISUCURSALID, FIFECHAID, FIORIGENID, FITOPID, FICONCEPTOID, FITOTOPERS, FITIEMPOTOTAL FROM TACJCXTACUMULA WHERE TACJCXTACUMULA.FIFECHAID=" + fecha;
        
        return usrcajaJdbcTemplate.query(sql, new obtenerAcumulaTest(), (Object[]) null);
    }

    class obtenerAcumulaTest implements RowMapper<CjCRPCxTAcumula> {

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

    private int parsearInteger(Object intg) {
        Integer tmpInteger = (Integer) intg;
        int parseado = tmpInteger.intValue();
        return parseado;

    }

    private CjCRPCxTAcumula configuracionTest(String prueba) {

        CjCRPCxTAcumula acumulaPrueba = new CjCRPCxTAcumula();
    
        if (prueba.equals("registro")) {
            //Insercion 
            acumulaPrueba = AsignarRegistro(519, 19990101, 1, 32, 0, 5, 500);
            fecha = 19990101;
        }
        if (prueba.equals("limpieza")) {
            //Limpieza         
          acumulaPrueba = AsignarRegistro(333, 18880101, 1, 33, 0, 6, 408);
            fecha = 18880101;
        }
        
        return acumulaPrueba;
    }

    public CjCRPCxTAcumula AsignarRegistro(Integer sucursalId, Integer fechaId, Integer origenId, Integer productoId,
            Integer conceptoId, Integer totalOperacionId, float tiempoTotal) {

        CjCRPCxTAcumula acumulaPrueba = new CjCRPCxTAcumula();
        acumulaPrueba.setSucursalId(sucursalId);
        acumulaPrueba.setFechaId(fechaId);
        acumulaPrueba.setOrigenId(origenId);
        acumulaPrueba.setProductoId(productoId);
        acumulaPrueba.setConceptoId(conceptoId);
        acumulaPrueba.setTotalOperacionId(totalOperacionId);
        acumulaPrueba.setTiempoTotal(tiempoTotal);

        return acumulaPrueba;

    }

    public static String registrolLimpiezaTest(CjCRPCxTAcumula acumulaPrueba) {
        return "INSERT INTO TACJCXTACUMULA  (FISUCURSALID,FIFECHAID, FIORIGENID, FITOPID, FICONCEPTOID, FITOTOPERS, FITIEMPOTOTAL, "
                + "FICALCULO, FIFECHACALCULO, FITIPOCOBRO, FIUNIDADID, FCUSERINSMOV, FDFECHAINSMOV, FCUSERMODIF, FDFECHAMODIF) "
                +"VALUES ("
                +acumulaPrueba.getSucursalId()+","+acumulaPrueba.getFechaId()+","
                + acumulaPrueba.getOrigenId()+","+acumulaPrueba.getProductoId()+","
                +acumulaPrueba.getConceptoId()+","+acumulaPrueba.getTotalOperacionId()+","
                +acumulaPrueba.getTiempoTotal()+","
                +"0, 0, 0, 0,'PRTxCTest', sysdate,'USRCAJADES', sysdate)";

    }
}
