package Conector;

import java.sql.*;
import java.util.*;

import def.*;

public class ConectorBD {

	private Connection conexion;
	private Statement statement;

	public ConectorBD(String host,String nombreBD, String usuario, String password) {
		//nombreBD = "SEGECA";
		//usuario = "admin";
		//password = "Grupo10";
		String url = "jdbc:mysql://"+ host + "/";
		String driver = "com.mysql.jdbc.Driver";

		try {
			Class.forName(driver).newInstance();
			conexion = DriverManager.getConnection(url+nombreBD,usuario,password);
			System.out.println("Estoy conectado!!!");
			statement = conexion.createStatement();
		} 
		catch (Exception e) {
			System.out.println("No se ha podido conectar a la base de datos.");
		}
		//Este metodo solo se le llama cuando se desean hacer las pruebas unitarias
		//pruebas.PU.PU_ConectorBD();
	}

	public void desconectar(){
		try{
			conexion.close();
			System.out.println("Ya no estoy conectado!!");
		}catch (Exception e){
			System.out.println("Excepción no controlada al desconectarse de la base de datos.");
		}
	}

	/*
	 * PERSONA
	 */
	public void addPersonaCCC(Persona persona){
		try{
			statement.executeUpdate("insert into `Personas` set telefono='"+persona.getTelefono()+
					"', email='"+persona.getEmail()+"', CCC='"+persona.getCcc().getNombreCCC()+"', nombre='"+
					persona.getNombre()+"', permisos='"+ persona.getPermisos() +"', `nick`='"+persona.getNick()+"';");
		}catch (Exception e){
			System.out.println("No se han podido introducir los datos de 'Persona' con éxito");
			System.out.println("La persona a introducir era: "+ persona.getNick());
		}
	}

	public void editPerson(Persona persona){
		try{
			statement.executeUpdate("update `Personas` set telefono="+persona.getTelefono()+
					", email='"+persona.getEmail()+"', nombre='"+ persona.getNombre()+
					"' where `nick`='"+persona.getNick()+"' limit 1;");
			if (persona.getCcc().getNombreCCC()!=null){
				statement.executeUpdate("update `Personas` set CCC='"+persona.getCcc().getNombreCCC()+"' where `nick`='"+persona.getNick()+"' limit 1;");                    
			}
			if (persona.getPermisos()!=null){
				statement.executeUpdate("update `Personas` set permisos='"+persona.getPermisos()+"' where `nick`='"+persona.getNick()+"' limit 1;");                    
			}
		}catch (Exception e){
			System.out.println("No se han podido introducir los datos de 'Persona' con éxito");
			System.out.println("La persona a introducir era: "+ persona.getNick());
		}
	}

	public void registroPersona(def.Persona persona){
		try{
			statement.executeUpdate("insert into `Personas` set `nick`='"+persona.getNick()+"', password='"+ persona.getPassword()+"';");

		}catch (Exception e){
			System.out.println("No se han podido registrar a"+ persona.getNick()+" con éxito");
		}
	}

	public void extractPersona(def.Persona persona){
		try{
			ResultSet resultadoPersona = statement.executeQuery("select * from Personas where nick='"+ persona.getNick() +"'");
			if (resultadoPersona.next()){
				def.Ccc ccc = new def.Ccc(resultadoPersona.getString("CCC"));
				persona.setCcc(ccc);
				persona.setEmail(resultadoPersona.getString("email"));
				persona.setNick(resultadoPersona.getString("nick"));
				persona.setPassword(resultadoPersona.getString("password"));
				persona.setNombre(resultadoPersona.getString("nombre"));
				persona.setPermisos(resultadoPersona.getString("permisos"));
				persona.setTelefono(resultadoPersona.getInt("telefono"));
			}
		}catch (Exception e){
			System.out.println("Error al intentar obtener la persona con nick "+ persona.getNick());
		}
	}
	public void deletePersonaCCC(String persona){
		try {
			statement.executeUpdate("delete from `Personas` where `nick`='"+ persona +"' limit 1");
		} catch (SQLException e) {
			System.out.println("Error al intentar eliminar la persona: " + persona);
		}
	}



	/*
	 * CCC
	 */
	public void createCCC(Ccc ccc){
		try{
			ResultSet resultado = statement.executeQuery("select * from CCC where nombre_CCC='"+ ccc.getNombreCCC() +"'");
			if (resultado.next()){//Si ya estaba este usuario actualizamos sus campos
				statement.executeUpdate("update `CCC` set secretario='"+ccc.getSecretario()+
						"', administrador='"+ccc.getAdministrador()+"', presidente='"+ccc.getPresidente()+
						"' where `nombre_CCC`='"+ccc.getNombreCCC()+"' limit 1;");
			}else{//Si no, lo introuducimos por primera vez
				statement.executeUpdate("insert into `CCC` set secretario='"+ccc.getSecretario()+
						"', administrador='"+ccc.getAdministrador()+"', presidente='"+ccc.getPresidente()+
						"', `nombre_CCC`='"+ccc.getNombreCCC()+"';");
			}
		}catch (Exception e){
			System.out.println("No se han podido introducir los datos de 'CCC' con éxito");
			System.out.println("El CCC a introducir era:\n"+ ccc.getNombreCCC());
		}
	}

	public void extractCCC(def.Ccc ccc){
		LinkedList<def.Persona> listaPersonas= new LinkedList<def.Persona>();
		LinkedList<def.Agenda> listaAgendas= new LinkedList<def.Agenda>();
		LinkedList<def.Pc> listaPc= new LinkedList<def.Pc>();
		try{
			ResultSet resultado = statement.executeQuery("select * from CCC where nombre_CCC='"+ ccc.getNombreCCC() +"'");
			if (resultado.next()){

				ccc.setAdministrador(resultado.getString("administrador"));
				ccc.setNombreCCC(resultado.getString("nombre_CCC"));
				ccc.setSecretario(resultado.getString("secretario"));
				ccc.setPresidente(resultado.getString("presidente"));
			}
			resultado = statement.executeQuery("select * from Personas where CCC='" + ccc.getNombreCCC() + "'");
			while(resultado.next()){
				def.Persona a = new def.Persona(resultado.getString("nick"));
				a.setCcc(ccc);
				listaPersonas.add(a);
			}			
			Iterator<Persona> personaIterator = listaPersonas.iterator();
			while(personaIterator.hasNext()){
				extractPersona(personaIterator.next());
			}

			resultado = statement.executeQuery("select * from Agenda where ccc='" + ccc.getNombreCCC() + "';");
			while(resultado.next()){
				def.Agenda a = new def.Agenda(resultado.getInt("cod_agenda"));
				a.setCcc(ccc);
				listaAgendas.add(a);
			}
			Iterator<Agenda> agendaIterator = listaAgendas.iterator();
			while(agendaIterator.hasNext()){
				extractAgenda(agendaIterator.next());
			}

			resultado = statement.executeQuery("select * from PC where CCC='" + ccc.getNombreCCC() + "';");
			while(resultado.next()){
				def.Pc a = new def.Pc(resultado.getInt("cod_PC"));
				a.setCcc(ccc);
				listaPc.add(a);
			}
			Iterator<Pc> pcIterator = listaPc.iterator();
			while(pcIterator.hasNext()){
				extractPc(pcIterator.next());
			}
			ccc.setPersonasCollection(listaPersonas);
			ccc.setAgendaCollection(listaAgendas);
			ccc.setPcCollection(listaPc);
		}catch (Exception e){
			System.out.println("Error al intentar obtener el CCC de nombre: "+ ccc.getNombreCCC());
		}
	}

	public void deleteCCC(String nombreCcc){
		try {
			statement.executeUpdate("delete from `CCC` where `nombre_CCC`='"+ nombreCcc +"' limit 1");
		} catch (SQLException e) {
			System.out.println("Error al intentar eliminar el CCC: " + nombreCcc);
		}
	}

	//Obtener listado de CCC
	public LinkedList<String> extraerListaCCC(){
		LinkedList<String> lista = new LinkedList<String>();
		try{
			ResultSet resultado = statement.executeQuery("select * from CCC");
			while(resultado.next()){
				lista.add(resultado.getString("nombre_CCC"));
			}
		}catch (Exception e){
			System.out.println("Error al intentar obtener el listado de CCC disponibles");
		}
		return lista;
	}

	/*
	 * AGENDA
	 */
	public void createAgenda(Agenda agenda){
		try{
			if (agenda.getCodAgenda() == null){
				statement.executeUpdate("insert into `Agenda` set lugar='"+agenda.getLugar()+"', proposito='"+agenda.getProposito()
						+"', fecha='"+agenda.getFecha()+"', participantes='"+agenda.getParticipantes()+"', hora_fin='"+agenda.getHoraFin()+"', hora_inicio='"
						+ agenda.getHoraInicio() +"', ccc='"+ agenda.getCcc().getNombreCCC()+"'");
			}else{
				ResultSet resultado = statement.executeQuery("select * from Agenda where cod_agenda='"+ agenda.getCodAgenda() +"'");
				if (resultado.next()){//Si ya estaba esta agenda actualizamos sus campos
					statement.executeUpdate("update `Agenda` set lugar='"+agenda.getLugar()+"', participantes='"+agenda.getParticipantes()+
							"', proposito='"+agenda.getProposito()+"', fecha='"+agenda.getFecha()+"', hora_fin='"+agenda.getHoraFin()
							+"', hora_inicio='"+ agenda.getHoraInicio()+"', ccc='"+ agenda.getCcc().getNombreCCC()
							+"' where `cod_agenda`='"+agenda.getCodAgenda()+"' limit 1;");
				}else{
					System.out.println("El codigo de Agenda todavía no existe en la base de Datos." +
							"\n Para crear una agenda nueva codAgenda debe de ser null.");
				}
			}
		}catch (Exception e){
			System.out.println("No se han podido introducir los datos de 'Agenda' con éxito");
			System.out.println("La agenda a introducir era: "+ agenda.toString());
		}
	}

	public void extractAgenda(def.Agenda agenda){
		try{
			ResultSet resultado = statement.executeQuery("select * from Agenda where cod_agenda='"+ agenda.getCodAgenda() +"'");
			if (resultado.next()){
				def.Ccc ccc = new def.Ccc(resultado.getString("ccc"));
				agenda.setCcc(ccc);
				agenda.setParticipantes(resultado.getString("participantes"));
				agenda.setCodAgenda(resultado.getInt("cod_agenda"));
				agenda.setFecha(resultado.getString("fecha"));
				agenda.setHoraFin(resultado.getString("hora_fin"));
				agenda.setHoraInicio(resultado.getString("hora_inicio"));
				agenda.setLugar(resultado.getString("lugar"));
				agenda.setProposito(resultado.getString("proposito"));		
			}
		}catch (Exception e){
			System.out.println("Error al intentar obtener la Agenda con codigo: "+ agenda.getCodAgenda());
		}
	}

	public void deleteAgenda(int codAgenda){
		try {
			statement.executeUpdate("delete from `Agenda` where `cod_agenda`='"+ codAgenda +"' limit 1");
		} catch (SQLException e) {
			System.out.println("Error al intentar eliminar la Agenda: " + codAgenda);
		}
	}

	/*
	 * PC
	 */

	public void addPC(Pc pc){
		try{
			statement.executeUpdate("insert into `PC` set descripcion='"+pc.getDescripcion()+"', fecha='"+pc.getFecha()
					+"', motivo='"+pc.getMotivo()+"', estado='"+pc.getEstado()+"', prioridad='"+pc.getPrioridad()+"', documentos='"+pc.getDocumentos()+"', email='"+pc.getEmail()
					+"'");
		}catch (Exception e){
			System.out.println("Error al intentar insertar la PC con motivo: '"+ pc.getMotivo()+"'");
		}
	}

	public void addPCagendaCCC(Pc pc){
		try{
			statement.executeUpdate("update `PC` set descripcion='"+pc.getDescripcion()+"', fecha='"+pc.getFecha()
					+"', motivo='"+pc.getMotivo()+"', estado='"+pc.getEstado().toString()+"', prioridad='"+pc.getPrioridad()+
					"', CCC='"+pc.getCcc().getNombreCCC()+"', agenda='"+pc.getAgenda().getCodAgenda()+
					"' where `cod_PC`='"+pc.getCodPC()+"' limit 1;");
		}catch (Exception e){
			System.out.println("Error al intentar insertar la PC con motivo: '"+ pc.getMotivo()+"'");
		}
	}

	public void valorarPC(Pc pc){
		try{
			statement.executeUpdate("update `PC` set valoracion='" +pc.getValoracion()+ "' where `cod_PC`='"+ pc.getCodPC()+"' limit 1;");
		}catch (Exception e){
			System.out.println("Error al intentar actualizar la valoración de la PC con id: '"+ pc.getCodPC()+"'");
		}
	}


	public void modEstadoPC(Pc pc){
		try{
			statement.executeUpdate("update `PC` set estado='" +pc.getEstado().toString() + "' where `cod_PC`='"+ pc.getCodPC() +"' limit 1;");
		}catch (Exception e){
			System.out.println("Error al intentar actualizar la valoración de la PC con id: '"+ pc.getCodPC()+"'");
		}
	}
	
	public void addPCaCCC(Pc pc){
		try{
			statement.executeUpdate("update `PC` set CCC='" +pc.getCcc().getNombreCCC() + "' where `cod_PC`='"+ pc.getCodPC() +"' limit 1;");
		}catch (Exception e){
			System.out.println("Error al intentar actualizar el CCC de la PC con id: '"+ pc.getCodPC()+"'");
		}
	}

	public void extractPc(Pc pc){
		try{
			ResultSet resultado = statement.executeQuery("select * from PC where cod_PC='"+ pc.getCodPC() +"'");
			if (resultado.next()){
				def.Ccc ccc = new def.Ccc(resultado.getString("CCC"));
				pc.setCcc(ccc);
				pc.setDescripcion(resultado.getString("descripcion"));
				pc.setDocumentos(resultado.getString("documentos"));
				pc.setEmail(resultado.getString("email"));
				Pc.Estado estado = Pc.Estado.valueOf(resultado.getString("estado"));
				pc.setEstado(estado);
				pc.setFecha(resultado.getString("fecha"));
				pc.setMotivo(resultado.getString("motivo"));
				pc.setPrioridad(resultado.getString("prioridad"));
				pc.setValoracion(resultado.getString("valoracion"));
				def.Agenda agenda = new def.Agenda(resultado.getInt("agenda"));
				extractAgenda(agenda);
				pc.setAgenda(agenda);
			}
		}catch (Exception e){
			System.out.println("Error al intentar obtener la PC con codigo: "+ pc.getCodPC());
		}
	}

	public void deletePc(int codPC){
		try {
			statement.executeUpdate("delete from `PC` where `cod_PC`='"+ codPC +"' limit 1");
		} catch (SQLException e) {
			System.out.println("Error al intentar eliminar la PC: " + codPC);
		}
	}

	/*
	 * ACTA
	 */
	public void createActa(Acta acta){
		try{
			if(acta.getCodActa() == null){
				statement.executeUpdate("insert into `Acta` set agenda='"+acta.getAgenda().getCodAgenda()+
						"', ausencias='"+acta.getAusencias()+"', resultados='"+acta.getResultados()
						+"'");
			}else{
				ResultSet resultado = statement.executeQuery("select * from Acta where cod_acta='"+ acta.getCodActa() +"'");
				if (resultado.next()){//Si ya estaba este acta actualizamos sus campos
					statement.executeUpdate("update `Acta` set agenda='"+acta.getAgenda().getCodAgenda()+
							"', ausencias='"+acta.getAusencias()+"', resultados='"+acta.getResultados()
							+"' where `cod_acta`='"+acta.getCodActa()+"' limit 1;");
				}else{
					System.out.println("El codigo de Acta todavía no existe en la base de Datos." +
							"\n Para crear un acta nueva codActa debe de ser null.");
				}
			}
		}catch (Exception e){
			System.out.println("No se han podido introducir los datos del Acta con éxito");
			System.out.println("El acta a introducir era: "+ acta.toString());
		}
	}
	public void extractActa(Acta acta){
		try{
			ResultSet resultado = statement.executeQuery("select * from Acta where cod_acta='"+ acta.getCodActa() +"'");
			if (resultado.next()){
				acta.setAusencias(resultado.getString("ausencias"));
				acta.setResultados(resultado.getString("resultados"));
				def.Agenda agenda = new def.Agenda(resultado.getInt("agenda"));
				acta.setAgenda(agenda);
				extractAgenda(agenda);
			}
		}catch (Exception E){
			System.out.println("Error al intentar obtener el Acta con codigo: "+ acta.getCodActa());
		}
	}

	public void deleteActa(int codActa){
		try {
			statement.executeUpdate("delete from `Acta` where `cod_acta`='"+ codActa +"' limit 1");
		} catch (SQLException e) {
			System.out.println("Error al intentar eliminar la Agenda: " + codActa);
		}
	}

	public int getCodLastAgenda(){
		ResultSet resultado;
		try {
			resultado = statement.executeQuery("select * from Agenda order by cod_agenda desc limit 1");
			if (resultado.next()){
				return resultado.getInt("cod_agenda");
			}
		} catch (SQLException e) {
			return 0;
		}
		return 0;
	}

	public int getCodLastActa(){
		ResultSet resultado;
		try {
			resultado = statement.executeQuery("select * from Acta order by cod_acta desc limit 1");
			if (resultado.next()){
				return resultado.getInt("cod_acta");
			}
		} catch (SQLException e) {
			return 0;
		}
		return 0;
	}

	public int getCodLastPc(){
		ResultSet resultado;
		try {
			resultado = statement.executeQuery("select * from PC order by cod_PC desc limit 1");
			if (resultado.next()){
				return resultado.getInt("cod_PC");
			}
		} catch (SQLException e) {
			return 0;
		}

		return 0;
	}
}
/*
 * Prioritario:
 * resto de deletes
 */
