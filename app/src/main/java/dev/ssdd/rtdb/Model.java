package dev.ssdd.rtdb;

public class Model {
    String xyz1,xyz2;

    public Model() {
    }

    public Model(String xyz1, String xyz2) {
        this.xyz1 = xyz1;
        this.xyz2 = xyz2;
    }

    public String getXyz1() {
        return xyz1;
    }

    public void setXyz1(String xyz1) {
        this.xyz1 = xyz1;
    }

    public String getXyz2() {
        return xyz2;
    }

    public void setXyz2(String xyz2) {
        this.xyz2 = xyz2;
    }
}
