import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ControlApollo11 {

    // Declaración de componentes de la interfaz gráfica
    private JPanel rootPanel;
    private JLabel lblSegundosSal;
    private JButton btnInicio;
    private JButton btnCancelar;
    private JProgressBar barraProgreso;
    private JFormattedTextField txtSegundosCuenta;

    // Declaración de variables relacionadas con la lógica del hilo
    private Task countdownTask;
    private MonitoringThread monitoringThread;
    private String txtMensaje = "FIN Proceso";
    private boolean hiloIniciado = false;
    private JLabel lblTituloPral;
    private JLabel lblsegundosentrada;
    private JLabel lblbarraprogreso;


    public ControlApollo11() {

// Configuración de acciones para los botones de Iniciar y Detener Cuenta Atras.
        btnInicio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!hiloIniciado) {
                    try {
                        int segundos = Integer.parseInt(txtSegundosCuenta.getText());
                        validarSegundos(segundos);

                        barraProgreso.setMaximum(segundos);
                        countdownTask = new Task(segundos);
                        monitoringThread = new MonitoringThread(countdownTask);

                        countdownTask.start();
                        monitoringThread.start();

                        txtMensaje = "Lanzamiento de transbordador realizado correctamente.";
                        hiloIniciado = true;
                    } catch (NumberFormatException ex) {
                        mostrarError("Debes introducir un número entero de segundos para la cuenta atras.");
                        inicializarObjetos();
                    } catch (IllegalArgumentException ex) {
                        mostrarError(ex.getMessage());
                        inicializarObjetos();
                    }
                } else {
                    mostrarError("Ya hay una cuenta atrás de lanzamiento en ejecución. Debes esperar a que finalice o cancelarla antes de iniciar otra.");
                }
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hiloIniciado) {
                    txtMensaje = "La cuenta atrás ha sido cancelada.Lanzamiento transbordador abortado.";
                    inicializarObjetos();
                    countdownTask.detener();
                    monitoringThread.detener();
                    hiloIniciado = false;
                } else {
                    mostrarError("La cuenta atrás no ha sido iniciada.Lanzamiento transbordador no iniciado.");
                }
            }
        });
    }

    public static void main(String[] args) {

        // Creación de la ventana principal
        JFrame frame = new JFrame("Control Misión Apollo 11");

        // Creación del panel principal que contendrá todos los componentes
        frame.setContentPane(new ControlApollo11().rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    // Método para validar que los segundos sean mayores que cero
    private void validarSegundos(int segundos) {
        if (segundos <= 0) {
            throw new IllegalArgumentException("El número de segundos de la cuenta atras debe ser mayor que cero.");
        }
    }

    // Método para mostrar mensajes de error
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje);
    }

    // Método para reiniciar los componentes al estado inicial
    private void inicializarObjetos() {
        barraProgreso.setValue(0);
        txtSegundosCuenta.setText("");
        lblSegundosSal.setText("");
    }

    // Clase que representa el hilo principal de la cuenta atrás
    private class Task extends Thread {
        private int numSegundos;
        private volatile boolean detenerHilo = false;

        public Task(int numSegundos) {
            this.numSegundos = numSegundos;
        }

        @Override
        public void run() {
            for (var ref = new Object() {
                int i = 1;
            }; ref.i <= numSegundos; ref.i++) {
                if (detenerHilo) {
                    break;
                }
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setValue(ref.i);
                    lblSegundosSal.setText(Integer.toString(numSegundos - ref.i));
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, txtMensaje);
                inicializarObjetos();
                hiloIniciado = false;
            });
        }

        public void detener() {
            detenerHilo = true;
            interrupt();
        }
    }

    // Clase que representa el hilo de monitoreo y registro del estado del hilo principal
    private class MonitoringThread extends Thread {
        private Task task;
        private List<String> registro;

        public MonitoringThread(Task task) {
            this.task = task;
            this.registro = new ArrayList<>();
        }

        @Override
        public void run() {
            while (task.isAlive()) {
                // Monitorear y registrar el estado del hilo principal
                String estado = task.detenerHilo ? "Hilo Cancelado" : "Hilo Activo";
                registro.add("Estado temporal del hilo principal: " +  estado);

                try {
                    // Dormir durante un intervalo antes de verificar de nuevo
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // Registro del estado final del hilo principal
            registro.add("Estado final del hilo principal: Completado");

            // Mostrar el registro en un cuadro de mensaje
            SwingUtilities.invokeLater(() -> {
                String registroCompleto = String.join("\n", registro);
                JOptionPane.showMessageDialog(null, "Registro del hilo principal :\n" + registroCompleto);
            });
        }

        // Método para detener el hilo de monitoreo
        public void detener() {
            interrupt();
        }
    }
}
