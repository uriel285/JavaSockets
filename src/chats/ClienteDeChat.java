package chats;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.util.Properties;
import java.io.*;

public class ClienteDeChat {

	private Socket conexion = null;
    private BufferedReader entradaDelServidor = null;
    private PrintStream salidaAlServidor = null;
    
    private JTextArea jtaMensajesEntrantes;
    private JScrollPane jspPanelTexto;
    private JTextField jtfMensajesAEnviar;
    private JButton bEnviar;
    private JButton bSalir;
    private JFrame marco;
    private JComboBox<String> nombresUsuarios;
    private JDialog jdAcercaDe;
    
    public ClienteDeChat() {
        jtaMensajesEntrantes = new JTextArea(10,50);
        jspPanelTexto = new JScrollPane(jtaMensajesEntrantes, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jtfMensajesAEnviar = new JTextField(50);
        bEnviar = new JButton("Enviar");
        bSalir = new JButton("Salir");
        nombresUsuarios = new JComboBox<String>();
        nombresUsuarios.addItem("PeRamirez");
        nombresUsuarios.addItem("nud39");
        nombresUsuarios.addItem("Flash");
    }
    
    public void mostrarMarco() {
        marco = new JFrame("Ventana Para Chat");
        
		// Usar el gestor de salidas BorderLayout para el frame
        marco.setLayout(new BorderLayout());
        
        marco.add(jspPanelTexto, BorderLayout.WEST);
        marco.add(jtfMensajesAEnviar, BorderLayout.SOUTH);
        
		// Crea el panel con los botones
        JPanel p1 = new JPanel();
        p1.setLayout(new GridLayout(3,1));
        p1.add(bEnviar);
        p1.add(bSalir);
        p1.add(nombresUsuarios);
        
		// Agregar el panel con los botones en el centro
        marco.add(p1, BorderLayout.CENTER);
 
        // Crear la barra y el men� Archivo
        JMenuBar jmb = new JMenuBar();
        JMenu jmArchivo = new JMenu("Archivo");
        JMenuItem jmiSalir = new JMenuItem("Salir");
        jmiSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        jmArchivo.add(jmiSalir);
        jmb.add(jmArchivo);
        marco.setJMenuBar(jmb);
        
        // Agregar la ayuda a la barra de men�
        JMenu jmAyuda = new JMenu("Ayuda");
        JMenuItem jmiAcercaDe = new JMenuItem("Acerca De...");
        jmiAcercaDe.addActionListener(new ManejadorAcercaDe());
        jmAyuda.add(jmiAcercaDe);
        jmb.add(jmAyuda);
        
        // Agregar el listener a los componentes apropiados
        bEnviar.addActionListener(new ManejadorEnviar());
        jtfMensajesAEnviar.addActionListener(new ManejadorEnviar());
        marco.addWindowListener(new ManejadorCerrar());
        bSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        marco.pack();
        marco.setVisible(true);
        
        realizarConexion();
    }
    
    private void realizarConexion() {
		FileInputStream in;
		Properties props = null;
		try {
			in = new FileInputStream("propCliente.txt");
			props = new Properties();
			props.load(in);
		} catch (FileNotFoundException e) {
			System.out.println("Archivo de propCliente no encontrado...");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("No se pudieron cargar las propiedades...");
			e.printStackTrace();
		}

        // Inicializar la informaci�n de la ip y el puerto del servidor
		String ipServidor= "127.0.0.1";
		int puertoServidor=3000;
        
        try {
            // Crear la conexi�n al servidor de chat
            conexion = new Socket(ipServidor, puertoServidor);
            // Preparar la corriente de entrada y almacenarla en una variable de instancia
            // Preparar la corriente de salida y almacenarla en una variable de instancia
            entradaDelServidor = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            salidaAlServidor = new PrintStream(conexion.getOutputStream());
            
            // Lanzar el thread de lectura
            LectorRemoto lectorRemoto = new LectorRemoto();
            lectorRemoto.run();
            
        } catch (Exception e) {
            System.err.println("Imposible conectarse al servidor!");
            e.printStackTrace();
        }
    }
    
    private class ManejadorEnviar implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String text = jtfMensajesAEnviar.getText();
            text = nombresUsuarios.getSelectedItem() + ": " + text + "\n";
            // Usar la corriente de salida obtenida en el socket y
            // almacenada en la variable salidaAlServidor para
            // invocar al m�todo print() y as� enviarle el trexto
            // al servidor
            salidaAlServidor.print(text);


            jtfMensajesAEnviar.setText("");
        }
    }
    
    private class ManejadorCerrar extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            exit();
        }
    }
    
    private void exit() {
        try {
            conexion.close();
        } catch (Exception e) {
            System.err.println("Error mientras se cerraba la conexi�n al servidor.");
        }
        System.exit(0);
    }
    
    private class ManejadorAcercaDe implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Crear un cuadro de di�logo cuando se lo requiera
            if ( jdAcercaDe == null ) {
                jdAcercaDe = new AcercaDeDialog(marco, "Acerca De...", true);
            }
            jdAcercaDe.setVisible(true);
        }
    }
    
 	private class AcercaDeDialog extends JDialog implements ActionListener  {
		private static final long serialVersionUID = 4688687119526400797L;

		public AcercaDeDialog(Frame padre, String titulo, boolean modal) {
            super(padre,titulo,modal);
            add(new JLabel("El cliente de chat es una herramienta que permite " +
                    "entablar conversaciones con otros clientes de chat via un servidor de chat"),BorderLayout.NORTH);
            JButton b = new JButton("Aceptar");
            add(b,BorderLayout.SOUTH);
            b.addActionListener(this);
            pack();
        }

        // Ocultar el cuadro de di�logo cuando se presiona Aceptar
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }   
    
    
    public static void main(String[] args) {
        ClienteDeChat c = new ClienteDeChat();
        c.mostrarMarco();
    }
    
    private class LectorRemoto implements Runnable{

		@Override
		public void run() {
			try {
				while (true) {
					String nextLine = entradaDelServidor.readLine();
					jtaMensajesEntrantes.append(nextLine + "/n");
					jtaMensajesEntrantes.setCaretPosition(jtaMensajesEntrantes.getDocument().getLength()-1);
					
				}
			} catch (Exception e) {
				System.out.println("Error mientras se leia el servidor");
				e.printStackTrace();
			}
		}
    	
    }
}
