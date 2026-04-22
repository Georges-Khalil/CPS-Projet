package fr.sorbonne_u.cps.pubsub.plugins;

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.pubsub.components.Broker;
import fr.sorbonne_u.cps.pubsub.connectors.RegistrationConnector;
import fr.sorbonne_u.cps.pubsub.exceptions.AlreadyRegisteredException;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownClientException;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.pubsub.interfaces.RegistrationCI.RegistrationClass;
import fr.sorbonne_u.cps.pubsub.plugins.interfaces.ClientRegistrationI;
import fr.sorbonne_u.cps.pubsub.ports.RegistrationOutboundPort;

/**
 * Plugin for handling client registration with the broker.
 * Creates a RegistrationOutboundPort and connects it to the broker's
 * RegistrationInboundPort, then delegates all registration calls.
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class ClientRegistrationPlugin
extends		AbstractPlugin
implements	ClientRegistrationI
{
	private static final long serialVersionUID = 1L;

	/** Default URI for this plugin. */
	public static final String PLUGIN_URI = "client-registration-plugin";

	/** The URI of the ReceivingInboundPort of the owner component. */
	protected String receptionPortURI;

	/** Outbound port connected to the broker's RegistrationInboundPort. */
	protected RegistrationOutboundPort registrationOutboundPort;

	/** The publishing port URI obtained after registration. */
	protected String publishingPortURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create the registration plugin.
	 *
	 * @param receptionPortURI	URI of the owner's ReceivingInboundPort.
	 */
	public ClientRegistrationPlugin(String receptionPortURI, boolean isSenderOnly) {
		super();
		this.setPluginURI(PLUGIN_URI);
		this.receptionPortURI = (isSenderOnly ? "#" : "") + receptionPortURI;
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	@Override
	public void installOn(ComponentI owner) throws Exception {
		super.installOn(owner);

		// Add the required interface
		this.addRequiredInterface(RegistrationCI.class);

		// Create the outbound port and publish it
		this.registrationOutboundPort = new RegistrationOutboundPort(this.getOwner());
		this.registrationOutboundPort.publishPort();

		// Connect to the broker's registration inbound port
		this.getOwner().doPortConnection(
				this.registrationOutboundPort.getPortURI(),
				Broker.BROKER_REGISTRATION_URI,
				RegistrationConnector.class.getCanonicalName()
		);
	}

	@Override
	public void finalise() throws Exception {
		if (this.registrationOutboundPort.connected()) {
			this.getOwner().doPortDisconnection(
					this.registrationOutboundPort.getPortURI());
		}
		super.finalise();
	}

	@Override
	public void uninstall() throws Exception {
		this.registrationOutboundPort.unpublishPort();
		this.registrationOutboundPort.destroyPort();
		this.removeRequiredInterface(RegistrationCI.class);
		super.uninstall();
	}

	// -------------------------------------------------------------------------
	// Accessors
	// -------------------------------------------------------------------------

	/**
	 * Return the receptionPortURI used to identify the client.
	 *
	 * @return	the URI of the reception port.
	 */
	public String getReceptionPortURI() {
		return this.receptionPortURI;
	}

	/**
	 * Return the publishing port URI obtained after registration.
	 *
	 * @return	the URI of the broker's publishing inbound port.
	 */
	public String getPublishingPortURI() {
		return this.publishingPortURI;
	}

	/**
	 * Return a reference to the registration outbound port.
	 * Used by other plugins that need access to registration services.
	 *
	 * @return	the RegistrationOutboundPort.
	 */
	public RegistrationOutboundPort getRegistrationOutboundPort() {
		return this.registrationOutboundPort;
	}

	// -------------------------------------------------------------------------
	// ClientRegistrationI methods
	// -------------------------------------------------------------------------

	@Override
	public boolean registered() {
		try {
			return this.registrationOutboundPort.registered(this.receptionPortURI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean registered(RegistrationClass rc) throws UnknownClientException {
		try {
			return this.registrationOutboundPort.registered(this.receptionPortURI, rc);
		} catch (UnknownClientException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void register(RegistrationClass rc) throws AlreadyRegisteredException {
		try {
			this.publishingPortURI = this.registrationOutboundPort.register(
					this.receptionPortURI, rc);
		} catch (AlreadyRegisteredException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void modifyServiceClass(RegistrationClass rc)
	throws UnknownClientException, AlreadyRegisteredException {
		try {
			this.publishingPortURI = this.registrationOutboundPort.modifyServiceClass(
					this.receptionPortURI, rc);
		} catch (UnknownClientException e) {
			throw e;
		} catch (AlreadyRegisteredException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void unregister() throws UnknownClientException {
		try {
			this.registrationOutboundPort.unregister(this.receptionPortURI);
			this.publishingPortURI = null;
		} catch (UnknownClientException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
