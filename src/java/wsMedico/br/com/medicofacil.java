/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wsMedico.br.com;

import java.sql.Time;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.joda.time.Minutes;


/**
 *
 * @author u14168
 */
@WebService(serviceName = "medicofacil")
public class medicofacil {

    /**
     * This is a sample web service operation
     */

     private AdaptedPreparedStatement bd;
     private Medicos medicos;
     private Horarios horarios;
     
     /**
     * Este método funciona como um construtor, criando uma conexão com o banco de dados
     * e inicializandoas váriaveis que farão acesso ao mesmo.
     **/
    @WebMethod(operationName = "conecta")
    public void conecta() throws Exception {
        
        this.bd = new AdaptedPreparedStatement("com.microsoft.sqlserver.jdbc.SQLServerDriver",
			            	           "jdbc:sqlserver://regulus:1433;databasename=BDu14168",
			            	           "BDu14168", "46424120858");  
        
        medicos = new Medicos(bd);
        horarios = new Horarios(bd);
    }
    
    /**
    * Este método verifica se existe um médico com o Crm e senha digitados, caso exista retorna true
    * caso contrário false.
    **/
    @WebMethod(operationName = "login")
    public boolean login(@WebParam(name = "crm") String crm, @WebParam(name = "senha") String senha) throws Exception {

      bd.prepareStatement("loginMedico_sp ?,?");      
      bd.setString(1, crm);
      bd.setString(2, senha);
      
      AdaptedResultSet registros = (AdaptedResultSet) bd.executeQuery();
      
      registros.first();
      if (registros.getInt("qtos") == 0)
        return false;
      
      return true;
    }
    
    /**
     * Este método altera os dados do médico passado como parâmetro, isto é utiliza-se o id como condição
     * de alteração, e todos os outros campos são alerados
     **/
    @WebMethod(operationName = "alterarMedico")
    public void alterarMedico(@WebParam(name = "medico") Medico medico) throws Exception {
      medicos.alterar(medico);
    }
    
    /**
     * Este método permite o médico cadastar vários horários
     **/
    @WebMethod(operationName = "cadastrarHorario")
    public void cadastrarHorario(@WebParam(name = "idMedico") int idMedico, @WebParam(name = "dia") String dia, Time inicio, int qtdConsultas, Time duracao, Time intervalo ) throws Exception {
      
        //o horário em que o médico irá iniciar seu atendiamento (em minutos)
        //por exemplo se o atendimento inicia-se às 10:00, então o inicioAtendimento
        //terá o valor 10*60 = 600 minutos
        int inicioAtendimento = inicio.getMinutes(); 
        
        //duração de uma consulta (em minutos)
        int duracaoConsulta = duracao.getMinutes();
        
        //o intervalo entre duas consultados
        int intervaloConsulta = intervalo.getMinutes();
        
        Horario horario;
        int inicioConsulta, fimConsulta;
        
        for(int i = 0; i < qtdConsultas; i++)
        {
             //é o horário em que começa a consulta (em minutos)             
             inicioConsulta = inicioAtendimento+i*(duracaoConsulta+intervaloConsulta);              
             
             //é o horário em que termina a consulta (em minutos)             
             fimConsulta = inicioConsulta+duracaoConsulta;           
                                                    
             //o horário formatado para inserir no banco de dados.
             //Observe que o inicioConsulta está sendo multiplicado por 60*1000, isso porque
             //o construtor recebe o tempo em milissegundos
             Time inicioAux = new Time(inicioConsulta*60*1000); 
             Time fimAux = new Time(fimConsulta*60*1000); 
             
             horario = new Horario(idMedico, dia, inicioAux, fimAux);
             horarios.cadastrar(horario);
        }                
    }
    
    @WebMethod(operationName = "alterarHorario")
    public void alterarHorario(@WebParam(name = "horario") Horario horario) throws Exception {
      horarios.alterar(horario);
    }

    @WebMethod(operationName = "pesquisarHorario")
    public arrayList<Horario> pesquisarHorario(@WebParam(name = "dia") String dia) throws Exception {
        
      if(dia == null || dia.trim().equals(""))
          return this.pesquisarHorario();
      
      //caso o dia seja segunda-feira, o abreviação sera SEG
      String abreviacao = dia.toUpperCase().substring(0, 3);
      
      return horarios.getHorarios("diaSemana = '"+abreviacao+"'");
    }

    @WebMethod(operationName = "pesquisarHorario")
    public arrayList<Horario> pesquisarHorario() throws Exception {        
      return horarios.getHorarios();
    }
}
