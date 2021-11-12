package me.tomassetti.javadocextractor;

import com.github.javaparser.ast.DataKey;

import java.util.Objects;

public class MyDataKey extends DataKey<String> {
    public MyDataKey(String keyName) {
        this.keyName = keyName;
    }

    public String keyName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MyDataKey myDataKey = (MyDataKey) o;
        return Objects.equals(keyName, myDataKey.keyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), keyName);
    }
}
