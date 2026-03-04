package fr.sorbonne_u.cps.pubsub.plugins;

import java.util.ArrayList;

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.pubsub.ports.RegistrationOutboundPort;
import fr.sorbonne_u.cps.pubsub.connectors.PublishingConnector;
import fr.sorbonne_u.cps.pubsub.connectors.PrivilegedClientConnector;
import fr.sorbonne_u.cps.pubsub.exceptions.UnauthorisedClientException;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownChannelException;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownClientException;
import fr.sorbonne_u.cps.pubsub.interfaces.MessageI;
import fr.sorbonne_u.cps.pubsub.interfaces.PublishingCI;
import fr.sorbonne_u.cps.pubsub.interfaces.PrivilegedClientCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI.RegistrationClass;
import fr.sorbonne_u.cps.pubsub.plugins.interfaces.ClientPublicationI;
import fr.sorbonne_u.cps.pubsub.ports.PublishingOutboundPort;
import fr.sorbonne_u.cps.pubsub.ports.PrivilegedClientOutboundPort;

/**
 * Plugin for handling client publication on the pub/sub system.
 * Creates a PublishingOutboundPort (or PrivilegedClientOutboundPort for
 * STANDARD/PREMIUM clients) and connects it to the broker's publishing
 * inbound port after registration.
 *
 * <p>This plugin needs access to the {@link ClientRegistrationPlugin} installed
 * on the same component to call channelExist and channelAuthorised, and to
 * obtain the publishing port URI after registration.</p>
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class ClientPublicationPlugin
extends		AbstractPlugin
implements	ClientPublicationI
{
	private static final long serialVersionUID = 1L;

	/** Default URI for this plugin. */
	public static final String PLUGIN_URI = "client-publication-plugin";

	/** The outbound port for publishing messages. */
	protected PublishingOutboundPort publishingOutboundPort;

	/** The service class of the client (FREE, STANDARD, PREMIUM). */
	protected RegistrationClass registrationClass;

	/** Whether the publication port has been connected. */
	protected boolean connected;

	/** Reference to the registration plugin on the same component. */
	protected ClientRegistrationPlugin registrationPlugin;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create the publication plugin.
	 *
	 * @param registrationClass	the service class of the client.
	 */
	public ClientPublicationPlugin(RegistrationClass registrationClass) {
		super();
		this.setPluginURI(PLUGIN_URI);
		this.registrationClass = registrationClass;
		this.connected = false;
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	@Override
	public void installOn(ComponentI owner) throws Exception {
		super.installOn(owner);

		// Create the appropriate outbound port depending on service class
		if (this.registrationClass == RegistrationClass.FREE) {
			this.addRequiredInterface(PublishingCI.class);
			this.publishingOutboundPort = new PublishingOutboundPort(this.getOwner());
		} else {
			this.addRequiredInterface(PrivilegedClientCI.class);
			this.publishingOutboundPort = new PrivilegedClientOutboundPort(this.getOwner());
		}
		this.publishingOutboundPort.publishPort();
	}

	@Override
	public void initialise() throws Exception {
		super.initialise();
	}

	/**
	 * Connect the publication outbound port to the broker's publishing
	 * inbound port. Must be called after registration.
	 *
	 * @param publishingPortURI	URI of the broker's publishing inbound port.
	 * @throws Exception		if the connection fails.
	 */
	public void connectToPublishingPort(String publishingPortURI) throws Exception {
		if (this.registrationClass == RegistrationClass.FREE) {
			this.getOwner().doPortConnection(
					this.publishingOutboundPort.getPortURI(),
					publishingPortURI,
					PublishingConnector.class.getCanonicalName()
			);
		} else {
			this.getOwner().doPortConnection(
					this.publishingOutboundPort.getPortURI(),
					publishingPortURI,
					PrivilegedClientConnector.class.getCanonicalName()
			);
		}
		this.connected = true;
	}

	@Override
	public void finalise() throws Exception {
		if (this.connected && this.publishingOutboundPort.connected()) {
			this.getOwner().doPortDisconnection(
					this.publishingOutboundPort.getPortURI());
			this.connected = false;
		}
		super.finalise();
	}

	@Override
	public void uninstall() throws Exception {
		this.publishingOutboundPort.unpublishPort();
		this.publishingOutboundPort.destroyPort();
		if (this.registrationClass == RegistrationClass.FREE) {
			this.removeRequiredInterface(PublishingCI.class);
		} else {
			this.removeRequiredInterface(PrivilegedClientCI.class);
		}
		super.uninstall();
	}

	// -------------------------------------------------------------------------
	// Accessors
	// -------------------------------------------------------------------------

	/**
	 * Return the publishing outbound port.
	 * Used by the PrivilegedClient plugin to access channel management methods.
	 *
	 * @return	the publishing outbound port.
	 */
	public PublishingOutboundPort getPublishingOutboundPort() {
		return this.publishingOutboundPort;
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
	// ClientPublicationI methods
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
	public void publish(String channel, MessageI message)
	throws UnknownClientException, UnknownChannelException, UnauthorisedClientException {
		try {
			String receptionPortURI = this.getRegistrationPlugin().getReceptionPortURI();
			this.publishingOutboundPort.publish(receptionPortURI, channel, message);
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
	public void publish(String channel, ArrayList<MessageI> messages)
	throws UnknownClientException, UnknownChannelException, UnauthorisedClientException {
		try {
			String receptionPortURI = this.getRegistrationPlugin().getReceptionPortURI();
			this.publishingOutboundPort.publish(receptionPortURI, channel, messages);
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
}
