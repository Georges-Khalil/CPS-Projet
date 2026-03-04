package fr.sorbonne_u.cps.pubsub.plugins;

import java.time.Duration;
import java.util.concurrent.Future;

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.pubsub.composants.ClientI;
import fr.sorbonne_u.cps.pubsub.exceptions.NotSubscribedChannelException;
import fr.sorbonne_u.cps.pubsub.exceptions.UnauthorisedClientException;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownChannelException;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownClientException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageFilterI;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.ReceivingCI;
import fr.sorbonne_u.cps.pubsub.plugins.interfaces.ClientSubscriptionI;
import fr.sorbonne_u.cps.pubsub.ports.ReceivingInboundPort;

/**
 * Plugin for handling client subscriptions and message reception.
 * Creates a ReceivingInboundPort that the broker will connect to for
 * sending messages to this client, and delegates subscription management
 * to the broker via the registration outbound port (shared with the
 * registration plugin).
 *
 * <p>This plugin needs access to the {@link ClientRegistrationPlugin} installed
 * on the same component for subscription management calls to the broker.</p>
 *
 * <p>When messages are received, they are forwarded to the owner component
 * which must implement {@link ClientI}.</p>
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class ClientSubscriptionPlugin
extends		AbstractPlugin
implements	ClientSubscriptionI
{
	private static final long serialVersionUID = 1L;

	/** Default URI for this plugin. */
	public static final String PLUGIN_URI = "client-subscription-plugin";

	/** The inbound port for receiving messages from the broker. */
	protected ReceivingInboundPort receivingInboundPort;

	/** The URI of the receiving inbound port. */
	protected String receivingPortURI;

	/** Reference to the registration plugin on the same component. */
	protected ClientRegistrationPlugin registrationPlugin;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create the subscription plugin.
	 *
	 * @param receivingPortURI	the URI to use for the ReceivingInboundPort,
	 *                          or null to auto-generate one.
	 */
	public ClientSubscriptionPlugin(String receivingPortURI) {
		super();
		this.setPluginURI(PLUGIN_URI);
		this.receivingPortURI = receivingPortURI;
	}

	/**
	 * Create the subscription plugin with auto-generated port URI.
	 */
	public ClientSubscriptionPlugin() {
		this(null);
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	@Override
	public void installOn(ComponentI owner) throws Exception {
		super.installOn(owner);

		// Add the offered interface
		this.addOfferedInterface(ReceivingCI.class);

		// Create the receiving inbound port
		if (this.receivingPortURI != null) {
			this.receivingInboundPort = new ReceivingInboundPort(
					this.receivingPortURI, this.getOwner());
		} else {
			this.receivingInboundPort = new ReceivingInboundPort(this.getOwner());
			this.receivingPortURI = this.receivingInboundPort.getPortURI();
		}
		this.receivingInboundPort.publishPort();
	}

	@Override
	public void initialise() throws Exception {
		super.initialise();
	}

	@Override
	public void finalise() throws Exception {
		super.finalise();
	}

	@Override
	public void uninstall() throws Exception {
		this.receivingInboundPort.unpublishPort();
		this.receivingInboundPort.destroyPort();
		this.removeOfferedInterface(ReceivingCI.class);
		super.uninstall();
	}

	// -------------------------------------------------------------------------
	// Accessors
	// -------------------------------------------------------------------------

	/**
	 * Return the URI of the receiving inbound port.
	 *
	 * @return	the URI of the receiving port.
	 */
	public String getReceivingPortURI() {
		return this.receivingPortURI;
	}

	/**
	 * Set the registration plugin reference.
	 * Must be called after both plugins are installed on the same component.
	 *
	 * @param registrationPlugin	the ClientRegistrationPlugin.
	 */
	public void setRegistrationPlugin(ClientRegistrationPlugin registrationPlugin) {
		this.registrationPlugin = registrationPlugin;
	}

	/**
	 * Get the registration plugin installed on the same component.
	 *
	 * @return	the ClientRegistrationPlugin.
	 */
	protected ClientRegistrationPlugin getRegistrationPlugin() {
		return this.registrationPlugin;
	}

	// -------------------------------------------------------------------------
	// ClientSubscriptionI - Signatures called by the owner component
	// -------------------------------------------------------------------------

	@Override
	public boolean channelExist(String channel) {
		try {
			return this.getRegistrationPlugin()
					.getRegistrationOutboundPort()
					.channelExist(channel);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean channelAuthorised(String channel)
	throws UnknownClientException, UnknownChannelException {
		try {
			ClientRegistrationPlugin regPlugin = this.getRegistrationPlugin();
			return regPlugin.getRegistrationOutboundPort()
					.channelAuthorised(regPlugin.getReceptionPortURI(), channel);
		} catch (UnknownClientException e) {
			throw e;
		} catch (UnknownChannelException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean subscribed(String channel)
	throws UnknownClientException, UnknownChannelException {
		try {
			ClientRegistrationPlugin regPlugin = this.getRegistrationPlugin();
			return regPlugin.getRegistrationOutboundPort()
					.subscribed(regPlugin.getReceptionPortURI(), channel);
		} catch (UnknownClientException e) {
			throw e;
		} catch (UnknownChannelException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void subscribe(String channel, MessageFilterI filter)
	throws UnknownClientException, UnknownChannelException, UnauthorisedClientException {
		try {
			ClientRegistrationPlugin regPlugin = this.getRegistrationPlugin();
			regPlugin.getRegistrationOutboundPort()
					.subscribe(regPlugin.getReceptionPortURI(), channel, filter);
		} catch (UnknownClientException e) {
			throw e;
		} catch (UnknownChannelException e) {
			throw e;
		} catch (UnauthorisedClientException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void unsubscribe(String channel)
	throws UnknownClientException, UnknownChannelException,
			UnauthorisedClientException, NotSubscribedChannelException {
		try {
			ClientRegistrationPlugin regPlugin = this.getRegistrationPlugin();
			regPlugin.getRegistrationOutboundPort()
					.unsubscribe(regPlugin.getReceptionPortURI(), channel);
		} catch (UnknownClientException e) {
			throw e;
		} catch (UnknownChannelException e) {
			throw e;
		} catch (UnauthorisedClientException e) {
			throw e;
		} catch (NotSubscribedChannelException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void modifyFilter(String channel, MessageFilterI filter)
	throws UnknownClientException, UnknownChannelException,
			UnauthorisedClientException, NotSubscribedChannelException {
		try {
			ClientRegistrationPlugin regPlugin = this.getRegistrationPlugin();
			regPlugin.getRegistrationOutboundPort()
					.modifyFilter(regPlugin.getReceptionPortURI(), channel, filter);
		} catch (UnknownClientException e) {
			throw e;
		} catch (UnknownChannelException e) {
			throw e;
		} catch (UnauthorisedClientException e) {
			throw e;
		} catch (NotSubscribedChannelException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// -------------------------------------------------------------------------
	// ClientSubscriptionI - Signatures called by the broker
	// -------------------------------------------------------------------------

	@Override
	public void receive(String channel, MessageI message) {
		try {
			((ClientI) this.getOwner()).receiveOne(channel, message);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void receive(String channel, MessageI[] messages) {
		try {
			((ClientI) this.getOwner()).receiveMultiple(channel, messages);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// -------------------------------------------------------------------------
	// ClientSubscriptionI - Advanced reception (section 3.5.3, not yet implemented)
	// -------------------------------------------------------------------------

	@Override
	public MessageI waitForNextMessage(String channel)
	throws UnknownClientException, UnknownChannelException,
			UnauthorisedClientException, NotSubscribedChannelException {
		throw new UnsupportedOperationException(
				"waitForNextMessage not yet implemented (section 3.5.3)");
	}

	@Override
	public MessageI waitForNextMessage(String channel, Duration d)
	throws UnknownClientException, UnknownChannelException,
			UnauthorisedClientException, NotSubscribedChannelException {
		throw new UnsupportedOperationException(
				"waitForNextMessage with timeout not yet implemented (section 3.5.3)");
	}

	@Override
	public Future<MessageI> getNextMessage(String channel)
	throws UnknownClientException, UnknownChannelException,
			UnauthorisedClientException, NotSubscribedChannelException {
		throw new UnsupportedOperationException(
				"getNextMessage not yet implemented (section 3.5.3)");
	}
}
