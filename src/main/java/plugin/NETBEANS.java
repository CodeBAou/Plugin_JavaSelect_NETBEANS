package plugin;
import java.util.ArrayList;
import codebaou.interfaces.I_Plugin;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.JButton;
import plugin.NETBEANS;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NETBEANS implements I_Plugin{
    
    //Variable del plugin
    private String PATH_JREJDK                 = null;
    private final String nombreCarpetaBusqueda = "netbeans";
    private final String nombreFicheroBusqueda = "netbeans.conf";
    private File ficheroConfiguracion          = null;
    //Variables para controlar los errores
    private int[] codes                        = new int[]{ 0,1,2,3,4,5,6};
    private String[] mensajes                  = new String[]{ 
        null, 
        "OK. Se ha cambiado la configuracion de netbeans.",
        "No se pudo encontrar el fichero con el path especificado. ",
        "Netbeans se debe configurar con un JDK y no JRE.",
        "Acceso denegado, se necesitan permisos de administrador.",
        "No se ha podido abrir el archivo.",
        "Se ha producido un error mientras se estaba leyendo el archivo."
    };
    
    public static void main(String[] args){}
    
    //FUNCIONES DE LA INTERFAZ 
    @Override
    public int mainAll( String PathNuevo ) {
        return 0;
    }

    @Override
    public int mainWin( String PathNuevo ) { 
        
        this.ficheroConfiguracion = BuscarWindows();
        
        if(  this.ficheroConfiguracion != null ) 
        {
            if( PathNuevo.toLowerCase().contains("jre") == false )
            {
                return ModificarConfiguracion( PathNuevo );
            }
            else
            {
                return 3;
            }       
        }
        else
        {
            return 2;
        }
    }

    @Override
    public int mainLinux( String Path ) 
    {
        return 0;
    }

    @Override
    public int mainMac( String Path ) 
    {
        return 0;
    }

    @Override
    public int mainFreeBSD( String Path ) 
    {
       return 0;
    }
    
  
    @Override
    public String MensajeCode(int code) {
        return mensajes[ code ];
    }
 
    
    // FUNCIONES COMPLEMENTARIAS PARA EL FUNCIONAMINETO DEL PLUGIN
    
    /** Recorre las particiones C:\\ D:\\ F:\\
    * LLama BuscarInCarpetaWindows() una vez por particion siempre que no se hara encontrado
    * la funcion llamada retorne null
    */
    private File BuscarWindows(){
        
        File fichero            = null;
        final String[] busqueda = new String[]{"C:\\"};
        
        for( String disco: busqueda )
        {
            fichero = BuscaCarpetaNetbeansWindows( disco );
        }

        return fichero;
    }
    
    /** Recorre las carpetas de la particion pasada por parametro y llama a
    *  Get_File_Conf(String pathBusqueda).
    */
    private File BuscaCarpetaNetbeansWindows( String particion ){
        
        File carpetaNetbeans    = null;
        final String[] busqueda = new String[]{ particion + "Program Files\\", particion + "Program Files (x86)\\" };
        
        for( String carpeta : busqueda )
        {
            carpetaNetbeans = BuscaCarpetaNetbeansWindows( new File( carpeta ) , this.nombreCarpetaBusqueda );
            if(carpetaNetbeans != null){ break; }
        }
        
        //Si se encuentro la carpeta se busca el fichero dentro de es carpeta
        if( carpetaNetbeans != null )
        {
            carpetaNetbeans = BuscarFicheroConfiguracionWindows( carpetaNetbeans );
        }
        
        return carpetaNetbeans;
    }
    
    /** Realiza una busqueda a partir de un filtro y devuelve el File objecto del elmento buscado 
    * o null si no lo encuentra.
    * El filtro se compone de 3 parametros  
    * File carpeta  : donde se realiza la busqueda
    * String BUSCAR : nombre del file que se esta buscando ( constains() )
    * @return File
    */
    private File BuscaCarpetaNetbeansWindows( File CARPETA , String BUSCAR){
        
        File resultado = null;
        
        if( CARPETA != null )      
        {
            File carpetaBusqueda  = CARPETA;
            int carpetasLeidas1   = 0;
            //Busqueda nivel 1
            for( File hijo: carpetaBusqueda.listFiles() )
            {
                
                if  
                        
                ( 
                    hijo.isDirectory() 
                    && 
                    hijo.getName().toLowerCase().contains( BUSCAR ) 
                )
                {
                    //CARPETA ENCONTRADA EN EL NIVEL 1
                    resultado = new File( hijo.getAbsolutePath() );
                    break;
                }
                
                else
                    
                { 
                    if( carpetasLeidas1 == carpetaBusqueda.listFiles().length )
                    { 
                        //Empieza busqueda en el nivel 2
                        int carpetasLeidas2 = 0; 
                        
                        for( File fleven1 : carpetaBusqueda.listFiles() )
                        {
                            for( File fleven2 : fleven1.listFiles() )
                            {
                                if
                                        
                                (   
                                    fleven2.isDirectory() 
                                    && 
                                    fleven2.getName().toLowerCase().contains( BUSCAR ) 
                                )
                                {
                                    //CARPETA ENCONTRADA EN EL NIVEL 2
                                    resultado = new File(  fleven2.getAbsolutePath() );
                                    break;
                                }
                                
                                else
                                    
                                {  
                                    if
                                            
                                    ( 
                                        fleven2.isDirectory()  
                                        &&    
                                        carpetasLeidas2 == fleven2.listFiles().length 
                                    )
                                        
                                    {
                                        //Empieza busqueda en el nivel 3
                                        int carpetasLeidas3 = 0;
                                        
                                        for( File fleven3 : fleven2.listFiles() )
                                        {
                                            for( File file: fleven3.listFiles() )
                                            {
                                                if
                                                        
                                                ( 
                                                    fleven3.isDirectory() 
                                                    && 
                                                    fleven3.getName().toLowerCase().contains( BUSCAR ) 
                                                )
                                                    
                                                {
                                                    //CARPETA ENCONTRADA EN EL NIVEL 3
                                                    resultado = new File( file.getAbsolutePath() );
                                                    break;
                                                } 
                                                carpetasLeidas3++;
                                            }
                                        }
                                    } 
                                }
                                carpetasLeidas2++;
                            }
                        }
                    }
                }  
                carpetasLeidas1++;
            }
        }
        return resultado;
    }
    
    //Busca el fichero de configuracion de netbeans
    private File BuscarFicheroConfiguracionWindows( File CARPETANETBEANS ){
        
        File fichero = null;
            //Leven 1
            for( File carpeta: CARPETANETBEANS.listFiles() )
            {
                if( carpeta.isDirectory() )
                {
                    //Leven 2
                    for( File carpeta2: carpeta.listFiles() )
                    {
                        if( carpeta2.isDirectory() )
                        {
                            //Leven 3
                            for( File carpeta3 : carpeta2.listFiles() )
                            {
                                if
                                ( 
                                    carpeta3.isFile() && carpeta3.getName().contains("netbeans") && carpeta3.getName().contains(".conf") 
                                    || 
                                    carpeta3.isFile() && carpeta3.getName().equals(this.nombreFicheroBusqueda)
                                )
                                {
                                    fichero = carpeta3;//RESULTADO
                                }
                            }
                        }
                        
                        else if      
                        ( 
                            carpeta2.getName().contains("netbeans") && carpeta2.getName().contains(".conf") 
                            || 
                            carpeta2.isFile() && carpeta2.getName().equals(this.nombreFicheroBusqueda)
                        )
                            
                        {
                            fichero = carpeta2; //RESULTADO
                        }
                    }
                }
                
                else if
                        
                ( 
                    carpeta.getName().contains("netbeans") && carpeta.getName().contains(".conf") 
                    || 
                    carpeta.isFile() && carpeta.getName().equals(this.nombreFicheroBusqueda) 
                )
                    
                {
                    fichero = carpeta; //RESULTADO
                }
            }
        return fichero;
    }
    
    /*Lee todas las lieneas del fichero de configuracion y modifica la linea del 
      jdk por el nuevo path pasado como parametro
    */
    private int ModificarConfiguracion( String PathNuevo ){

        ArrayList<String> lineas = new ArrayList();
        String pathAux           = this.ficheroConfiguracion.getAbsolutePath();
        
        try      //LECTURA         
        {
            String cadena; 
            FileReader f     = new FileReader(this.ficheroConfiguracion); 
            BufferedReader b = new BufferedReader(f); 

            while( (cadena = b.readLine())!= null ) { 
                if( cadena.contains( "netbeans_jdkhome" ) )
                {
                    lineas.add( ModificaLineaConf( cadena, '=', PathNuevo ) );
                }else{
                    lineas.add( cadena );
                }
          
            } 
            
            b.close();
            f.close();
            
        } catch ( FileNotFoundException ex ) { 
                return 5;
                
        } catch ( IOException ex ) {
                return 6;
        }
       
        try     //ESCRITURA
            
        {
            
            FileWriter fw = new FileWriter( this.ficheroConfiguracion );
            BufferedWriter br = new BufferedWriter( fw );
            
            for( String cadena: lineas){
                br.write(cadena);
                br.newLine();
            }
            
            br.close();
            fw.close();
            
        }  catch (IOException ex) {
                return 4;
        }
        return 1;
        
    }
    
    //Retorna la linea de configuracion indicada modificada con el nuevo valor, para volver escribir en el fichero
    private String ModificaLineaConf( String LINEA, char LIMITADOR, String NUEVOVALOR ){
        String[] claveValor =   LINEA.split( "\\"+LIMITADOR );
        claveValor[1]       = "="+'"'+NUEVOVALOR+'"';
        return claveValor[0] + claveValor[1];
    }
    
    //Ecribe un nuevo fichero de configuracion
    private int NewFileConf(String[] lineas){
        
        File fichero  = null;
        FileWriter fw = null;
        
        try 
        {
            fichero           = new File("");
            fw                = new FileWriter( fichero );
            BufferedWriter bw = new BufferedWriter(fw);
            
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(NETBEANS.class.getName()).log(Level.SEVERE, null, ex);
             return 6;
        } 
        finally 
        {
            try {
                fw.close();
                return 1;
            } catch (IOException ex) {
                return 6;
            }
        }
    }
    
    private void deleteFile(){
        if(this.ficheroConfiguracion != null)
        {
            this.ficheroConfiguracion.delete();
        }
    }
 
}
