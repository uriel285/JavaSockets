package chats;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ServidorDeChat {

	public static void main(String[] args) {
		ServidorDeChat sc = new ServidorDeChat();
		sc.comenzar();
	}

	public final static int PUERTO_POR_DEFECTO = 2000;
	public final static int BACKLOG_POR_DEFECTO = 5;
	public final static int CONEXIONES_MAXIMAS_POR_DEFECTO = 20;
	public final static String ARCHIVO_HOST__POR_DEFECTO = "hosts.txt";
	public final static String ARCHIVO_DE_SONIDO_POR_DEFECTO = "file:gong.au";
	public final static String FRASE = "Esta es la clave";

	private String frase = FRASE;

	private int puerto = PUERTO_POR_DEFECTO;
	private int backlog = BACKLOG_POR_DEFECTO;
	private int numeroDeConexiones = 0;
	private int conexionesMaximas = CONEXIONES_MAXIMAS_POR_DEFECTO;
	private String archivoDeHosts = ARCHIVO_HOST__POR_DEFECTO;
	private String archivoDeSonido = ARCHIVO_DE_SONIDO_POR_DEFECTO;
	private List<Conexiones> conexiones = null;
	private AudioClip sonidoConexiones = null;
	private Map<String, String> MapaDelServidorAlCliente = null;

	//
	// M�todos para la clase Conexiones
	//

	String getFraseDeAcceso() {
		return frase;
	}

	String getNombreDelCliente(String servidor) {
		return MapaDelServidorAlCliente.get(servidor);
	}

	void enviarATodosLosClientes(String mensaje) {
		for (Conexiones c : conexiones) {
			c.sendMessage(mensaje);
		}
	}

	void reproducirSonido() {
		if (sonidoConexiones != null) {
			sonidoConexiones.play();
		}
	}

	synchronized void cerrarConexiones(Conexiones conexion) {
		conexiones.remove(conexion);
		numeroDeConexiones--;
		notify();
	}

	//
	// M�todos Encapsulados
	//

	private void comenzar() {

		FileInputStream in;
		Properties props = null;
		try {
			in = new FileInputStream("Propiedades.txt");
			props = new Properties();
			props.load(in);
		} catch (FileNotFoundException e) {
			System.out.println("Archivo de propiedades no encontrado...");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("No se pudieron cargar las propiedades...");
			e.printStackTrace();
		}

		String stringDelPuerto = props.getProperty("puerto");
		if (stringDelPuerto != null)
			puerto = Integer.parseInt(stringDelPuerto);

		String stringDelBacklog = props.getProperty("backlog");
		if (stringDelBacklog != null)
			backlog = Integer.parseInt(stringDelBacklog);

		String stringDelArchivoHosts = props.getProperty("archivoDeHosts");
		if (stringDelArchivoHosts != null)
			archivoDeHosts = stringDelArchivoHosts;

		String stringArchivoDeSonido = props.getProperty("archivoDeSonido");
		if (stringArchivoDeSonido != null)
			archivoDeSonido = stringArchivoDeSonido;

		String stringDeFrase = props.getProperty("frase");
		if (stringDeFrase != null)
			frase = stringDeFrase;

		String conexiones = props.getProperty("conexiones");
		if (conexiones != null)
			conexionesMaximas = Integer.parseInt(conexiones);

		this.conexiones = new ArrayList<Conexiones>(conexionesMaximas);
		this.MapaDelServidorAlCliente = new HashMap<String, String>(15);

		System.out.println("Configuraci�n del servidor:\n\tPuerto=" + puerto
				+ "\n\tBacklog m�ximo=" + backlog + "\n\tConexiones m�ximas="
				+ conexionesMaximas + "\n\tArchivo de Hosts=" + archivoDeHosts
				+ "\n\tArchivo de sonido=" + archivoDeSonido);

		crearListaDeServidores();

		try {
			URL sonido = new URL(archivoDeSonido);
			sonidoConexiones = Applet.newAudioClip(sonido);
			reproducirSonido();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Principio del ciclo de procesamiento
		procesarRequerimientos();
	}

	private void crearListaDeServidores() {
		File archivoHosts = new File(archivoDeHosts);
		try {
			System.out
					.println("Intentando leer el archivo de servidores hosts.txt: ");
			FileInputStream fis = new FileInputStream(archivoHosts);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String lineaLeida = null;
			while ((lineaLeida = br.readLine()) != null) {
				int spaceIndex = lineaLeida.indexOf(' ');
				if (spaceIndex == -1) {
					System.out
							.println("L�nea inv�lida en el archivo de hosts: "
									+ lineaLeida);
					continue;
				}
				String servidor = lineaLeida.substring(0, spaceIndex);
				String cliente = lineaLeida.substring(spaceIndex + 1);
				System.out.println("Le�do: " + cliente + "@" + servidor);
				MapaDelServidorAlCliente.put(servidor, cliente);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void procesarRequerimientos() {
		ServerSocket serverSocket = null;
		Socket socketDeComunicacion;

		try {
			System.out.println("Intentando iniciar el servidor...");
			serverSocket = new ServerSocket(puerto, backlog);
		} catch (IOException e) {
			System.out
					.println("Error al iniciar el servidor: no se puede abrir el puerto "
							+ puerto);
			e.printStackTrace();
			reproducirSonido();
			System.exit(-1);
		}
		System.out.println("Servidor iniciado en el puerto " + puerto);

		// Ejecutar el ciclo de escuchar / recibir infinitamente
		while (true) {
			try {
				// Esperar en este punto a que entre una conexi�n
				socketDeComunicacion = serverSocket.accept();
				manejarConexion(socketDeComunicacion);
			} catch (Exception e) {
				System.out.println("Incapaz de generar un socket hijo.");
				e.printStackTrace();
			}
		}
	}

	private synchronized void manejarConexion(Socket sktDeConexion) {
		while (numeroDeConexiones == conexionesMaximas) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		numeroDeConexiones++;
		Conexiones con = new Conexiones(this, sktDeConexion);
		Thread t = new Thread(con);
		t.start();
		conexiones.add(con);
	}
}
