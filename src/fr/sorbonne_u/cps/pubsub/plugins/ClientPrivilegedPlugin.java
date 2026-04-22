package fr.sorbonne_u.cps.pubsub.plugins;

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.pubsub.exceptions.AlreadyExistingChannelException;
import fr.sorbonne_u.cps.pubsub.exceptions.ChannelQuotaExceededException;
import fr.sorbonne_u.cps.pubsub.exceptions.UnauthorisedClientException;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownChannelException;
import fr.sorbonne_u.cps.pubsub.exceptions.UnknownClientException;
import fr.sorbonne_u.cps.pubsub.interfaces.PrivilegedClientCI;
import fr.sorbonne_u.cps.pubsub.interfaces.PrivilegedClientI;
import fr.sorbonne_u.cps.pubsub.ports.PrivilegedClientOutboundPort;

/**
 * Plugin for handling privileged client operations (channel management).
 * Delegates to the PrivilegedClientOutboundPort created by the
 * {@link ClientPublicationPlugin} when the client is STANDARD or PREMIUM.
 *
 * <p>This plugin requires both {@link ClientRegistrationPlugin} and
 * {@link ClientPublicationPlugin} (with STANDARD or PREMIUM class)
 * to be installed on the same component.</p>
 *
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class ClientPrivilegedPlugin
extends		AbstractPlugin
implements	PrivilegedClientI
{
	private static final long serialVersionUID = 1L;

	/** Default URI for this plugin. */
	public static final String PLUGIN_URI = "client-privileged-plugin";

	/** Reference to the registration plugin on the same component. */
	protected ClientRegistrationPlugin registrationPlugin;

	/** Reference to the publication plugin on the same component. */
	protected ClientPublicationPlugin publicationPlugin;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create the privileged client plugin.
	 */
	public ClientPrivilegedPlugin() {
		super();
		this.setPluginURI(PLUGIN_URI);
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods
	// -------------------------------------------------------------------------

	@Override
	public void installOn(ComponentI owner) throws Exception {
		super.installOn(owner);
		// No extra interfaces or ports needed; we reuse the publication plugin's
		// PrivilegedClientOutboundPort.
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
		super.uninstall();
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	/**
	 * Set the registration and publication plugin references.
	 * Must be called after all plugins are installed on the same component.
	 *
	 * @param registrationPlugin	the ClientRegistrationPlugin.
	 * @param publicationPlugin	the ClientPublicationPlugin.
	 */
	public void setPluginReferences(
			ClientRegistrationPlugin registrationPlugin,
			ClientPublicationPlugin publicationPlugin) {
		this.registrationPlugin = registrationPlugin;
		this.publicationPlugin = publicationPlugin;
	}

	/**
	 * Get the PrivilegedClientOutboundPort from the publication plugin.
	 *
	 * @return	the PrivilegedClientOutboundPort.
	 */
	protected PrivilegedClientOutboundPort getPrivilegedPort() {
		return (PrivilegedClientOutboundPort) this.publicationPlugin.getPublishingOutboundPort();
	}

	/**
	 * Get the registration plugin.
	 *
	 * @return	the ClientRegistrationPlugin.
	 */
	protected ClientRegistrationPlugin getRegistrationPlugin() {
		return this.registrationPlugin;
	}

	// -------------------------------------------------------------------------
	// PrivilegedClientI methods
	// -------------------------------------------------------------------------

	@Override
	public boolean hasCreatedChannel(String channel)
	throws UnknownClientException, UnknownChannelException {
		try {
			String uri = this.getRegistrationPlugin().getReceptionPortURI();
			return this.getPrivilegedPort().hasCreatedChannel(uri, channel);
		} catch (UnknownClientException e) {
			throw e;
		} catch (UnknownChannelException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean channelQuotaReached() throws UnknownClientException {
		try {
			String uri = this.getRegistrationPlugin().getReceptionPortURI();
			return this.getPrivilegedPort().channelQuotaReached(uri);
		} catch (UnknownClientException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void createChannel(String channel, String autorisedUsers)
	throws UnknownClientException, AlreadyExistingChannelException,
			ChannelQuotaExceededException {
		try {
			String uri = this.getRegistrationPlugin().getReceptionPortURI();
			this.getPrivilegedPort().createChannel(uri, channel, autorisedUsers);
		} catch (UnknownClientException e) {
            throw e;
		} catch (AlreadyExistingChannelException e) {
            throw e;
		} catch (ChannelQuotaExceededException e) {
            throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isAuthorisedUser(String channel, String uri)
	throws UnknownClientException, UnknownChannelException {
        return true; // TODO : Why is that method present in PrivilegedClientI and not PrivilegedClientCI ?
//		try {
//			return this.getPrivilegedPort().isAuthorisedUser(channel, uri);
//		} catch (UnknownClientException e) {
//			throw e;
//		} catch (UnknownChannelException e) {
//			throw e;
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
	}

	@Override
	public void modifyAuthorisedUsers(String channel, String autorisedUsers)
	throws UnknownClientException, UnknownChannelException,
			UnauthorisedClientException {
		try {
			String uri = this.getRegistrationPlugin().getReceptionPortURI();
			this.getPrivilegedPort().modifyAuthorisedUsers(uri, channel, autorisedUsers);
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
	public void destroyChannel(String channel)
	throws UnknownClientException, UnknownChannelException,
			UnauthorisedClientException {
		try {
			String uri = this.getRegistrationPlugin().getReceptionPortURI();
			this.getPrivilegedPort().destroyChannel(uri, channel);
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
	public void destroyChannelNow(String channel)
	throws UnknownClientException, UnknownChannelException,
			UnauthorisedClientException {
		try {
			String uri = this.getRegistrationPlugin().getReceptionPortURI();
			this.getPrivilegedPort().destroyChannelNow(uri, channel);
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
