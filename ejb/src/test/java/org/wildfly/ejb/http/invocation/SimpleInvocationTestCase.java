package org.wildfly.ejb.http.invocation;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClient;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.StatelessEJBLocator;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.river.RiverMarshallerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.ejb.http.HttpEJBReceiver;
import org.wildfly.ejb.http.TestEJBHandler;
import org.wildfly.ejb.http.TestEJBInvocation;
import org.wildfly.ejb.http.TestServer;
import org.wildfly.ejb.http.common.EchoBean;
import org.wildfly.ejb.http.common.EchoRemote;
import org.xnio.OptionMap;

/**
 * @author Stuart Douglas
 */
@RunWith(TestServer.class)
public class SimpleInvocationTestCase {

    public static final String APP = "my-app";
    public static final String MODULE = "my-module";

    @Test
    public void test() throws URISyntaxException {
        TestServer.setHandler(new TestEJBHandler() {
            @Override
            public Object handle(TestEJBInvocation invocation) {
                System.out.println(invocation);
                return invocation.getParams()[0];
            }
        });
        final StatelessEJBLocator<EchoRemote> statelessEJBLocator = new StatelessEJBLocator<EchoRemote>(EchoRemote.class, APP, MODULE, EchoBean.class.getSimpleName(), "");
        final EchoRemote proxy = EJBClient.createProxy(statelessEJBLocator);
        final String message = "Hello World!!!";
        final EJBClientContext ejbClientContext = EJBClientContext.create();
        MarshallerFactory factory = new RiverMarshallerFactory();
        ejbClientContext.registerEJBReceiver(new HttpEJBReceiver("node", new URI(TestServer.getDefaultServerURL()), TestServer.getWorker(), TestServer.getBufferPool(), null, OptionMap.EMPTY, factory, new HttpEJBReceiver.ModuleID(APP, MODULE, null)));
        final ContextSelector<EJBClientContext> oldClientContextSelector = EJBClientContext.setConstantContext(ejbClientContext);
        try {
            final String echo = proxy.echo(message);
            Assert.assertEquals("Unexpected echo message", message, echo);
        } finally {
            EJBClientContext.setSelector(oldClientContextSelector);
        }
    }

}
