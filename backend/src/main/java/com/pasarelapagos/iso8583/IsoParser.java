package com.pasarelapagos.iso8583;

import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

@Component
public class IsoParser {

    private final IsoMessageFactory isoMessageFactory;

    public IsoParser(IsoMessageFactory isoMessageFactory) {
        this.isoMessageFactory = isoMessageFactory;
    }

    public ISOMsg unpack(byte[] mensaje) throws Exception {

        ISOMsg isoMsg = new ISOMsg();

        isoMsg.setPackager(
                isoMessageFactory.getPackager()
        );

        isoMsg.unpack(mensaje);

        return isoMsg;
    }

    public byte[] pack(ISOMsg mensaje) throws Exception {
        return mensaje.pack();
    }
}