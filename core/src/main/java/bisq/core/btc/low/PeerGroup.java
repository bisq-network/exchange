package bisq.core.btc.low;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.params.RegTestParams;

import org.bitcoinj.net.BlockingClientManager;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.net.discovery.DnsDiscovery;

import bisq.core.btc.nodes.ProxySocketFactory;
import bisq.core.btc.nodes.LocalBitcoinNode;

import bisq.common.config.Config;

import java.net.Proxy;
import java.net.InetSocketAddress;

import com.runjva.sourceforge.jsocks.protocol.Socks5Proxy;

final public class PeerGroup extends PeerGroupProxy {

    private PeerGroup(
            NetworkParameters params,
            BlockChain vChain
    ) {
        super(new org.bitcoinj.core.PeerGroup(params, vChain));
    }

    private PeerGroup(
            NetworkParameters params,
            BlockChain vChain,
            BlockingClientManager blockingClientManager
    ) {
        super(new org.bitcoinj.core.PeerGroup(params, vChain, blockingClientManager));
    }

    public static PeerGroup createPeerGroup(
            NetworkParameters params,
            BlockChain vChain,
            LocalBitcoinNode localBitcoinNode,
            Socks5Proxy socks5Proxy,
            int torSocketTimeout,
            int torVersionExchangeTimeout
    ) {
        /* Below algorithm tries to satisfy following requirements:
         * - Using a local BTC node implies not using a proxy.
         * - When using a proxy, a blocking client must be used.
         * - We only want to use a blocking client when we're using a proxy.
         * - BitcoinJ uses a local BTC node by default. Thus we disable its use
         *   when we're told it shouldn't be used.
         */
        if (localBitcoinNode.shouldBeUsed()) {
            return createLocalPeerGroup(params, vChain);
        } else {
            return createRemotePeerGroup(params, vChain,
                    socks5Proxy, torSocketTimeout, torVersionExchangeTimeout);
        }
    }

    private static PeerGroup createLocalPeerGroup(
            NetworkParameters params,
            BlockChain vChain
    ) {
        return new PeerGroup(params, vChain);
    }

    private static PeerGroup createRemotePeerGroup(
            NetworkParameters params,
            BlockChain vChain,
            Socks5Proxy socks5Proxy,
            int torSocketTimeout,
            int torVersionExchangeTimeout
    ) {
        PeerGroup peerGroup;
        var notUsingProxy = socks5Proxy == null;
        if (notUsingProxy) {
            peerGroup = new PeerGroup(params, vChain);
        } else {
            Proxy proxy = new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress(
                        socks5Proxy.getInetAddress().getHostName(),
                        socks5Proxy.getPort()
                        ));
            ProxySocketFactory proxySocketFactory =
                new ProxySocketFactory(proxy);
            BlockingClientManager blockingClientManager =
                new BlockingClientManager(proxySocketFactory);

            peerGroup = new PeerGroup(params, vChain, blockingClientManager);

            blockingClientManager.setConnectTimeoutMillis(torSocketTimeout);
            peerGroup.setConnectTimeoutMillis(torVersionExchangeTimeout);
        }
        // Keep remote PeerGroup from using a local BTC node.
        peerGroup.setUseLocalhostPeerWhenPossible(false);
        return peerGroup;
    }

    public void setupPeerAddressesOrDiscovery(
            PeerAddress[] peerAddresses,
            int numConnectionsForBtc,
            NetworkParameters params,
            PeerDiscovery discovery
    ) {
        if (peerAddresses != null) {
            for (PeerAddress addr : peerAddresses) this.addAddress(addr);
            int maxConnections = Math.min(numConnectionsForBtc, peerAddresses.length);
            //log.info("We try to connect to {} btc nodes", maxConnections);
            this.setMaxConnections(maxConnections);
            this.setAddPeersFromAddressMessage(false);
            peerAddresses = null;
        } else if (!params.equals(RegTestParams.get())) {
            this.addPeerDiscovery(discovery != null ? discovery : new DnsDiscovery(params));
        }

    }

}
