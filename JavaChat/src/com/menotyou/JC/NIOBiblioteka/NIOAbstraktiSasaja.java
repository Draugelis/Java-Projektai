package com.menotyou.JC.NIOBiblioteka;

import java.net.InetSocketAddress;

/**
 * Valdiklis (Interface) NIOAbstraktiSasaja kurio savybes paveldės
 * tiek NIOServerioSasaja tiek NIOSasaja.
 */
public interface NIOAbstraktiSasaja {

    void uzdaryk();

    InetSocketAddress gaukAdresa();

    boolean atidarytas();

    String gaukIp();

    int gaukPorta();

    Object gaukZyme();

    void nustatykZyme(Object zyme);
}
