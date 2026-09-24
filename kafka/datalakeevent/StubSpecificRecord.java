package com.ing.bankguarantees.remote.kafka.datalakeevent;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;

public class StubSpecificRecord implements SpecificRecord {
    private final Schema schema;
    private final Object[] values;

    public StubSpecificRecord(Schema schema) {
        this.schema = schema;
        this.values = new Object[schema.getFields().size()];
    }

    @Override
    public void put(int i, Object v) {
        values[i] = v;
    }

    @Override
    public Object get(int i) {
        return values[i];
    }

    @Override
    public Schema getSchema() {
        return schema;
    }
}
