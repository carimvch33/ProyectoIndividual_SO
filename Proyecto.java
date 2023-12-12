import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

class Proceso{ //clase que representa un proceso en el sistema
    private static int contador_id = 0; //lleva la cuenta de la cantidad total de instancias
    private int id; //identificador unico del proceso
    private int tiempo; //tiempo de ejecución
    private int memoria; //cantidad de memoria necesaria
    private int prioridad; //prioridad que se establecerá aleatoriamente durante la creación del proceso

    public Proceso(){
        this.id = ++contador_id;
        Random random = new Random();
        this.tiempo = random.nextInt(10) + 1; //tiempo aleatorio entre 1 y 10 unidades
        this.memoria = random.nextInt(100) + 1; //memoria aleatoria entre 1 y 100 unidades
        this.prioridad = random.nextInt(5) + 1; //prioridad aleatoria entre 1 y 5
    }

    public int getId(){
        return id;
    }

    public int getTiempo(){
        return tiempo;
    }

    public int getMemoria(){
        return memoria;
    }

    public int getPrioridad(){
        return prioridad;
    }
}

class CPU{ //clase que simula el comportamiento del CPU
    private Proceso proceso_actual;

    //'synchronized' se asegura de que un solo hijo pueda ejecutar estos metodos a la vez
    public synchronized void asignarProceso(Proceso proceso){ //asigna un proceso a la CPU
        this.proceso_actual = proceso;
        //imprimir mensaje en la consola
        System.out.println("Proceso " + proceso.getId() + " asignado a la CPU.");
    }

    public synchronized void liberarProceso(){ //libera el proceso actualmente asignado
        System.out.println("Proceso " + proceso_actual.getId() + " liberado.");
        this.proceso_actual = null;
    }
}

//clase que representa la memoria del sistema y gestiona los procesos en cola
class Memoria{
    public Queue<Proceso> obtenerColaProcesos(){
        return cola_de_procesos;
    }

    private int capacidad_maxima;
    private Queue<Proceso> cola_de_procesos;

    public Memoria(int capacidad_maxima){
        this.capacidad_maxima = capacidad_maxima;
        this.cola_de_procesos = new LinkedList<>();
    }

    //nuevamente uso 'synchronized' para evitar problemas de concurrencia
    public synchronized boolean agregarProceso(Proceso proceso){ //agrega un proceso a la memoria si hay espacio disponible
        try{
            if(cola_de_procesos.size() < capacidad_maxima){
                cola_de_procesos.add(proceso);
                System.out.println("Proceso " + proceso.getId() + " llegó a la memoria.");
                return true; //se agregó correctamente
            }
            else{
                throw new RuntimeException("No hay espacio para el proceso " + proceso.getId() + ".");
            }
        }
        catch(RuntimeException e){
            System.out.println("Error al agregar proceso: " + e.getMessage());
            return false;
        }
    }
    
    //Priority Scheduling (Planificación por prioridad)
    public synchronized Proceso obtenerProceso(){ //devolverá un obj de tipo Proceso, solo ejecutar un hilo a la vez.
        Proceso procesoConMayorPrioridad = null;

        for(Proceso proceso : cola_de_procesos){
            if(procesoConMayorPrioridad == null || proceso.getPrioridad() > procesoConMayorPrioridad.getPrioridad()){ //verifica si es la primera iteración
                procesoConMayorPrioridad = proceso; //se actuliza con el proceso actual
            }
        }

        if(procesoConMayorPrioridad != null){ //verifica si se encontró algun proceso con prioridad
            cola_de_procesos.remove(procesoConMayorPrioridad); //lo elimina de la cola de procesos
        }

        return procesoConMayorPrioridad; //finalmente se devuelve el proceso.
    }
}

//interfaz gráfica de usuario para el simulador
class InterfazGrafica extends JFrame{
    private JTextArea logArea;
    private JPanel cpuPanel;
    private JPanel memoriaPanel;

    public InterfazGrafica(){
        setTitle("Simulador de Asignación de CPU y Memoria");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logArea = new JTextArea();
        logArea.setEditable(false);

        cpuPanel = new JPanel();
        cpuPanel.setBackground(Color.GRAY);

        memoriaPanel = new JPanel();
        memoriaPanel.setLayout(new GridLayout(5, 1));
        memoriaPanel.setBackground(Color.LIGHT_GRAY);

        JScrollPane scrollPanel = new JScrollPane(logArea);

        add(cpuPanel, BorderLayout.NORTH);
        add(memoriaPanel, BorderLayout.CENTER);
        add(scrollPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void agregarLog(String mensaje){
        logArea.append(mensaje + "\n");
    }
    
    public void actualizarCPU(Proceso proceso){
        if(proceso != null){
            cpuPanel.setBackground(Color.RED);
        }
        else{
            cpuPanel.setBackground(Color.GRAY);
        }
    }

    public void actualizarMemoria(Queue<Proceso> cola_de_procesos){
        memoriaPanel.removeAll();

        if(cola_de_procesos != null){
            List<Proceso> lista_de_procesos = new ArrayList<>(cola_de_procesos);

            for(Proceso proceso : lista_de_procesos){
                JPanel procesoPanel = new JPanel();
                procesoPanel.setBackground(Color.blue);
                procesoPanel.setBorder(BorderFactory.createLineBorder(Color.black));
                procesoPanel.setPreferredSize(new Dimension(50, 50));
                memoriaPanel.add(procesoPanel);
            }
        }
        memoriaPanel.revalidate();
        memoriaPanel.repaint();
    }
}

//clase que implementa la lógica del sistema operativo simulado
class SistemaOperativo implements Runnable{
    private CPU cpu;
    private Memoria memoria;
    private InterfazGrafica gui;
    private int ejecuciones_maximas;
    private int ejecuciones_actuales;
    private boolean cpuOcupada;

    public SistemaOperativo(CPU cpu, Memoria memoria, InterfazGrafica gui, int ejecuciones_maximas){
        this.cpu = cpu;
        this.memoria = memoria;
        this.gui = gui;
        this.ejecuciones_maximas = ejecuciones_maximas;
        this.ejecuciones_actuales = 0;
    }

    public boolean cpuEstaOcupada(){
        return cpuOcupada;
    }

    //controla la ejecución del SO
    public void controladorEjecuciones(){
        while(ejecuciones_actuales < ejecuciones_maximas){
            try{
                Thread.sleep(100); //el hilo esperará n milisegundos antes de continuar con la siguiente iteración
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            ejecuciones_actuales++;
        }
    }

    @Override
    public void run(){ //controla el flujo principal del simulador
        Thread control_de_hilo = new Thread(this::controladorEjecuciones); //se crea un nuevo hilo que controlará la cantidad total de ejecuciones
        control_de_hilo.start(); //se inicia el hilo

        while(true){ 
            Proceso proceso = new Proceso(); //crea un nuevo proceso
            if(memoria.agregarProceso(proceso)){ //lo agrega a la memoria

                //dar o asignar cpu y liberar los procesos
                cpuOcupada = true; //cpu ocupada
                cpu.asignarProceso(proceso); //en caso de ser posible, lo asigna a la CPU

                try{
                    Thread.sleep(proceso.getTiempo() * 100); //tiempo de ejecución (que tanto tarde en llegar)
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }

            cpu.liberarProceso();
            cpuOcupada = false; //cpu libre y sin ocupar

            //actualizar la interfaz gráfica con la información del proceso y memoria
            SwingUtilities.invokeLater(() -> {
                gui.actualizarCPU(proceso);
                gui.actualizarMemoria(memoria.obtenerColaProcesos());
            });

            ejecuciones_actuales++;
        }
    }
}

//clase principal que inicia el simulador del SO
public class Proyecto{
    public static void main(String[] args){
        CPU cpu = new CPU();
        Memoria memoria = new Memoria(10); //capacidad máxima de memoria
        InterfazGrafica gui = new InterfazGrafica();
        SistemaOperativo sistemaOperativo = new SistemaOperativo(cpu, memoria, gui, 10);
        Thread HiloSO = new Thread(sistemaOperativo);

        HiloSO.start();
    }
}