/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 -                                                                            -
 - Copyright 2021  The AndroidVideoLib Authors:                               -
 -       https://github.com/hlvs-apps/AndroidVideoLib/blob/master/AUTHORS.md  -
 - Unless otherwise noted, this is                                            -
 - Licensed under the Apache License, Version 2.0 (the "License");            -
 - you may not use this file except in compliance with the License.           -
 - You may obtain a copy of the License at                                    -
 -                                                                            -
 -     http://www.apache.org/licenses/LICENSE-2.0                             -
 -                                                                            -
 - Unless required by applicable law or agreed to in writing, software        -
 - distributed under the License is distributed on an "AS IS" BASIS,          -
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   -
 - See the License for the specific language governing permissions and        -
 - limitations under the License.                                             -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

import android.os.Parcel;
import android.os.Parcelable;

import org.jcodec.common.model.RationalLarge;
import org.jetbrains.annotations.NotNull;

/**
 * copied from JCode and made Parcelable
 */
public class Rational extends org.jcodec.common.model.Rational implements Parcelable {
    private final int num,den;
    public Rational(int num, int den) {
        super(num, den);
        this.num=num;
        this.den=den;
    }

    protected Rational(Parcel in) {
        super(in.readInt(),in.readInt());
        num = super.getNum();
        den = super.getDen();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(num);
        dest.writeInt(den);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Rational> CREATOR = new Creator<Rational>() {
        @Override
        public Rational createFromParcel(Parcel in) {
            return new Rational(in);
        }

        @Override
        public Rational[] newArray(int size) {
            return new Rational[size];
        }
    };

    @Override
    public int getNum() {
        return super.getNum();
    }

    @Override
    public int getDen() {
        return super.getDen();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int multiplyS(int val) {
        return super.multiplyS(val);
    }

    @Override
    public int divideS(int val) {
        return super.divideS(val);
    }

    @Override
    public int divideByS(int val) {
        return super.divideByS(val);
    }

    @Override
    public long multiplyLong(long val) {
        return super.multiplyLong(val);
    }

    @Override
    public long divideLong(long val) {
        return super.divideLong(val);
    }

    @Override
    public org.jcodec.common.model.Rational flip() {
        return super.flip();
    }

    @Override
    public boolean smallerThen(org.jcodec.common.model.Rational sec) {
        return super.smallerThen(sec);
    }

    @Override
    public boolean greaterThen(org.jcodec.common.model.Rational sec) {
        return super.greaterThen(sec);
    }

    @Override
    public boolean smallerOrEqualTo(org.jcodec.common.model.Rational sec) {
        return super.smallerOrEqualTo(sec);
    }

    @Override
    public boolean greaterOrEqualTo(org.jcodec.common.model.Rational sec) {
        return super.greaterOrEqualTo(sec);
    }

    @Override
    public boolean equalsRational(org.jcodec.common.model.Rational other) {
        return super.equalsRational(other);
    }

    @Override
    public org.jcodec.common.model.Rational plus(org.jcodec.common.model.Rational other) {
        return super.plus(other);
    }

    @Override
    public RationalLarge plusLarge(RationalLarge other) {
        return super.plusLarge(other);
    }

    @Override
    public org.jcodec.common.model.Rational minus(org.jcodec.common.model.Rational other) {
        return super.minus(other);
    }

    @Override
    public RationalLarge minusLarge(RationalLarge other) {
        return super.minusLarge(other);
    }

    @Override
    public org.jcodec.common.model.Rational plusInt(int scalar) {
        return super.plusInt(scalar);
    }

    @Override
    public org.jcodec.common.model.Rational minusInt(int scalar) {
        return super.minusInt(scalar);
    }

    @Override
    public org.jcodec.common.model.Rational multiplyInt(int scalar) {
        return super.multiplyInt(scalar);
    }

    @Override
    public org.jcodec.common.model.Rational divideInt(int scalar) {
        return super.divideInt(scalar);
    }

    @Override
    public org.jcodec.common.model.Rational divideByInt(int scalar) {
        return super.divideByInt(scalar);
    }

    @Override
    public org.jcodec.common.model.Rational multiply(org.jcodec.common.model.Rational other) {
        return super.multiply(other);
    }

    @Override
    public RationalLarge multiplyLarge(RationalLarge other) {
        return super.multiplyLarge(other);
    }

    @Override
    public org.jcodec.common.model.Rational divide(org.jcodec.common.model.Rational other) {
        return super.divide(other);
    }

    @Override
    public RationalLarge divideLarge(RationalLarge other) {
        return super.divideLarge(other);
    }

    @Override
    public org.jcodec.common.model.Rational divideBy(org.jcodec.common.model.Rational other) {
        return super.divideBy(other);
    }

    @Override
    public RationalLarge divideByLarge(RationalLarge other) {
        return super.divideByLarge(other);
    }

    @Override
    public float scalar() {
        return super.scalar();
    }

    @Override
    public int scalarClip() {
        return super.scalarClip();
    }

    @Override
    public @NotNull String toString() {
        return super.toString();
    }

    @Override
    public double toDouble() {
        return super.toDouble();
    }
}
