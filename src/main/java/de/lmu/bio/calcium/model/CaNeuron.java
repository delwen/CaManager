package de.lmu.bio.calcium.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

public class CaNeuron extends CaGroup {
    private double age;
    private String comment;
    private String region;
    private String commonFilePrefix;
    private String sex;

    public CaNeuron(String name) {
        super(name);
    }

    public String getName() {
        return (String) getUserObject();
    }

    public void setName(String value) {
        setUserObject(value);
    }

    public double getAge() {
        return age;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean allowImages() {
        return true;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCommonFilePrefix() {
        return commonFilePrefix;
    }

    public void setCommonFilePrefix(String commonFilePrefix) {
        this.commonFilePrefix = commonFilePrefix;
    }

    public ArrayList<CaImage> getImages(boolean ascending) {
        ArrayList<CaImage> imgList = new ArrayList<CaImage>();
        Enumeration en = this.breadthFirstEnumeration();

        Object o;
        while (en.hasMoreElements()) {
            o = en.nextElement();
            if (!(o instanceof CaImage)) {
                continue;
            }

            CaImage image = (CaImage) o;
            imgList.add(image);
        }

        final int descending = ascending ? -1 : 1;

        Collections.sort(imgList, new Comparator<CaImage>() {
            @Override
            public int compare(CaImage a, CaImage b) {
                long dta = a.getMTime();
                long dtb = b.getMTime();

                return descending * (int) (dtb - dta);
            }

        });

        return imgList;
    }
}
