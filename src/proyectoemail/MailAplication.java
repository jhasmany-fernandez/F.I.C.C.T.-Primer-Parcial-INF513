/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectoemail;

import Negocio.NCasosBrigadas;

import Comunicacion.MailVerificationThread;
import Comunicacion.SendEmailThread;
import Conexion.IEmailEventListener;
import Metodos.CasoBrigada;
import Metodos.CasoHospital;
import Metodos.Enfermedad;
import Metodos.Estado;
import Metodos.Mapa;
import Metodos.PuntoAtencion;
import Metodos.Reportes;
import Metodos.Sintoma;
import Metodos.TipoPunto;
import Metodos.Usuarios;
import Negocio.NCasosHospitales;
import interpreter.analex.Interpreter;
import interpreter.analex.interfaces.ITokenEventListener;
import interpreter.analex.utils.Token;
import interpreter.events.TokenEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import Utils.Email;
import Utils.Handler;
import Utils.HtmlBuilder;
import Utils.Validator;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author
 */
public class MailAplication implements IEmailEventListener, ITokenEventListener {

    private MailVerificationThread mailVerificationThread;

    private CasoHospital casoHospital;
    private CasoBrigada casoBrigada;
    private Mapa mapa;
    private Usuarios usuario;
    private Reportes reportes;
    private Enfermedad enfermedad;
    private Estado estado;
    private Sintoma sintoma;
    private TipoPunto tipoPunto;
    private PuntoAtencion puntoAtencion;

    public MailAplication() {
        mailVerificationThread = new MailVerificationThread();
        mailVerificationThread.setEmailEventListener(MailAplication.this);
        casoBrigada = new CasoBrigada();
        casoHospital = new CasoHospital();
        mapa = new Mapa();
        usuario = new Usuarios();
        reportes = new Reportes();
        enfermedad = new Enfermedad();
        estado = new Estado();
        sintoma = new Sintoma();
        tipoPunto = new TipoPunto();
        puntoAtencion = new PuntoAtencion();
    }

    public void start() throws InterruptedException {
        Thread thread = new Thread(mailVerificationThread);
        thread.setName("Mail Verfication Thread");
        thread.start();
        //-------eliminar------ ya está add
        /*
        List<Email> em = new ArrayList<>();
        String coma = "casobrigada agregar [2001-02-01; 2023-12-02; detalleUWU; 1; 6; 4; 2]";
        //String coma = "mapa agregar [mapa1UU; detalle de mapa 2X; 10.57; 19.457; 1; 2; 3; ]";
        em.add(new Email("fernandocarrasc591@gmail.com", coma, "Hulala"));
        
        onReceiveEmailEvent(em);*/
        //-------hasta aqui----
    }

    @Override
    public void onReceiveEmailEvent(List<Email> emails) {
        System.out.println("onReceiveEmailEvent()");
        for (Email email : emails) {

            Interpreter interpreter = new Interpreter(email.getSubject(), email.getFrom());
            interpreter.setListener(MailAplication.this);
            Thread thread = new Thread(interpreter);
            thread.setName("Interpreter Thread");
            thread.start();
        }
    }

    @Override
    public void error(TokenEvent event) {
        Handler.handleError(event.getAction(), event.getSender(), event.getParams(), "");
    }

    @Override
    public void mapas(TokenEvent event) {
        System.out.println("mapa() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.ADD:
                mapa.agregar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.MODIFY:
                mapa.modificar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.DELETE:
                mapa.eliminar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.GET:
                mapa.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.REPORT:
                mapa.listar((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    @Override
    public void casohospital(TokenEvent event) {
        System.out.println("casohospital() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.ADD:
                casoHospital.agregar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.MODIFY:
                casoHospital.modificar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.DELETE:
                casoHospital.eliminar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.GET:
                casoHospital.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.REPORT:
                casoHospital.listar((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    @Override
    public void casobrigada(TokenEvent event) {
        System.out.println("casobrigada() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.ADD:
                casoBrigada.agregar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.MODIFY:
                casoBrigada.modificar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.DELETE:
                casoBrigada.eliminar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.GET:
                casoBrigada.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.REPORT:
                casoBrigada.listar((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    @Override
    public void usuario(TokenEvent event) {
        System.out.println("usuario() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.ADD:
                usuario.agregar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.MODIFY:
                usuario.modificar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.DELETE:
                usuario.eliminar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.GET:
                usuario.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.REPORT:
                System.out.println("ENTREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                usuario.listar((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    @Override
    public void hospital(TokenEvent event) {
        System.out.println("Hospitales() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.ADD:
                puntoAtencion.agregar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.MODIFY:
                puntoAtencion.modificar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.DELETE:
                puntoAtencion.eliminar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.GET:
                puntoAtencion.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.REPORT:
                puntoAtencion.listar((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    @Override
    public void enfermedad(TokenEvent event) {
        System.out.println("enfermedad() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.ADD:
                enfermedad.agregar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.MODIFY:
                enfermedad.modificar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.DELETE:
                enfermedad.eliminar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.GET:
                enfermedad.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.REPORT:
                enfermedad.listar((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    @Override
    public void analisis(TokenEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reportes(TokenEvent event) {
        System.out.println("reporte() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.GET:
                reportes.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    @Override
    public void tipoPunto(TokenEvent event) {
        System.out.println("tipoPuntos() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.ADD:
                tipoPunto.agregar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.MODIFY:
                tipoPunto.modificar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.DELETE:
                tipoPunto.eliminar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.GET:
                tipoPunto.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;    
            case Token.REPORT:
                tipoPunto.listar((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    @Override
    public void sintomas(TokenEvent event) {
        System.out.println("sintomas() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.ADD:
                sintoma.agregar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.MODIFY:
                sintoma.modificar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.DELETE:
                sintoma.eliminar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.GET:
                sintoma.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.REPORT:
                sintoma.listar((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    @Override
    public void estados(TokenEvent event) {
        System.out.println("estados() -> " + event.getAction());
        switch (event.getAction()) {
            case Token.ADD:
                estado.agregar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.MODIFY:
                estado.modificar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.DELETE:
                estado.eliminar((ArrayList<String>) event.getParams(), event.getSender());
                break;
            case Token.GET:
                estado.ver((ArrayList<String>) event.getParams(), event.getSender());
                break;    
            case Token.REPORT:
                estado.listar((ArrayList<String>) event.getParams(), event.getSender());
                break;
        }
    }

    public void gruposc(TokenEvent event) {
        Email emailObject = new Email(event.getSender(), "GRUPO 13 SC", HtmlBuilder.ContenidoHelp());
        Email.sendEmail(emailObject);
    }

}
