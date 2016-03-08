package pt.lsts.neptus.plugins.rosbridge;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.ros.RosRun;
import org.ros.concurrent.CancellableLoop;
import org.ros.internal.loader.CommandLineLoader;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.GpsFix;
import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.neptus.comm.manager.imc.ImcId16;
import pt.lsts.neptus.comm.manager.imc.ImcMsgManager;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.console.ConsolePanel;
import pt.lsts.neptus.i18n.I18n;

public class RosBridge extends ConsolePanel implements NodeMain {

    private static final long serialVersionUID = -7313376603901499787L;
    
    private Publisher<std_msgs.Float32> pubpos;
    private EstimatedState lastStateStuff = new EstimatedState();
   
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava/talker");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        final Publisher<std_msgs.String> publisher =
                connectedNode.newPublisher("chatter", std_msgs.String._TYPE);
        pubpos = connectedNode.newPublisher("estimated_state", std_msgs.Float32._TYPE);
        // This CancellableLoop will be canceled automatically when the node shuts
        // down.
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            private int sequenceNumber;

            @Override
            protected void setup() {
                sequenceNumber = 0;
            }

            @Override
            protected void loop() throws InterruptedException {
                std_msgs.String str = publisher.newMessage();
                str.setData("Hello world! " + sequenceNumber);
                publisher.publish(str);
                sequenceNumber++;
                Thread.sleep(1000);
                onNavSts();
            }
        });
    }
    
    public void onNavSts()
    {
        lastStateStuff.setSrc(0x0F02);
        lastStateStuff.setSrcEnt(46);
        lastStateStuff.setLat(1.0);
        lastStateStuff.setLon(1.1);
        send(lastStateStuff);
        System.out.println("ROSBRIDGE: Send faked state.");
    }

    public RosBridge(ConsoleLayout console) {
        super(console);
    }

    @Override
    public void cleanSubPanel() {
        // TODO Auto-generated method stub

    }

    @Override
    public void initSubPanel() {
        // TODO Auto-generated method stub
        CommandLineLoader loader = new CommandLineLoader(Lists.newArrayList(""));
        String nodeClassName = loader.getNodeClassName();
        System.out.println("Loading node class: " + loader.getNodeClassName());
        NodeConfiguration nodeConfiguration = loader.build();
        NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
        nodeMainExecutor.execute(this, nodeConfiguration);
    }
    
    @Subscribe
    public void on(EstimatedState msg) {
        // From any system
        System.out.println("ROSBRIDGE: Received estimated state.");
        std_msgs.Float32 north = pubpos.newMessage();
        north.setData((float) msg.getX());
        pubpos.publish(north);
        lastStateStuff = msg;
    }

    /* (non-Javadoc)
     * @see org.ros.node.NodeListener#onError(org.ros.node.Node, java.lang.Throwable)
     */
    @Override
    public void onError(Node arg0, Throwable arg1) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.ros.node.NodeListener#onShutdown(org.ros.node.Node)
     */
    @Override
    public void onShutdown(Node arg0) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.ros.node.NodeListener#onShutdownComplete(org.ros.node.Node)
     */
    @Override
    public void onShutdownComplete(Node arg0) {
        // TODO Auto-generated method stub

    }

}
