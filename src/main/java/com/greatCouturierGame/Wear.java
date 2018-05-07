package com.greatCouturierGame;

public class Wear {
    private int wearType;
    private int wearColor;
    private int wearTexture;
    private int wearTextureColor;
    private int wearTexture2;
    private int wearTextureColor2;
    private int wearTextureParams;
    private int wearTextureParams2;

    private Wear(Builder builder) {
        this.wearType = builder.wearType;
        this.wearColor = builder.wearColor;
        this.wearTexture = builder.wearTexture;
        this.wearTextureColor = builder.wearTextureColor;
        this.wearTexture2 = builder.wearTexture2;
        this.wearTextureColor2 = builder.wearTextureColor2;
        this.wearTextureParams = builder.wearTextureParams;
        this.wearTextureParams2 = builder.wearTextureParams2;
    }

    public int getWearType() {
        return wearType;
    }

    public int getWearColor() {
        return wearColor;
    }

    public int getWearTexture() {
        return wearTexture;
    }

    public int getWearTextureColor() {
        return wearTextureColor;
    }

    public int getWearTexture2() {
        return wearTexture2;
    }

    public int getWearTextureColor2() {
        return wearTextureColor2;
    }

    public int getWearTextureParams() {
        return wearTextureParams;
    }

    public int getWearTextureParams2() {
        return wearTextureParams2;
    }

    public static class Builder {
        private int wearType;
        private int wearColor;
        private int wearTexture;
        private int wearTextureColor;
        private int wearTexture2;
        private int wearTextureColor2;
        private int wearTextureParams;
        private int wearTextureParams2;

        public Builder(int wearType, int wearColor) {
            this.wearType = wearType;
            this.wearColor = wearColor;
        }

        public Builder setWearType(int wearType) {
            this.wearType = wearType;
            return this;
        }

        public Builder setWearColor(int wearColor) {
            this.wearColor = wearColor;
            return this;
        }

        public Builder setWearTexture(int wearTexture) {
            this.wearTexture = wearTexture;
            return this;
        }

        public Builder setWearTextureColor(int wearTextureColor) {
            this.wearTextureColor = wearTextureColor;
            return this;
        }

        public Builder setWearTexture2(int wearTexture2) {
            this.wearTexture2 = wearTexture2;
            return this;
        }

        public Builder setWearTextureColor2(int wearTextureColor2) {
            this.wearTextureColor2 = wearTextureColor2;
            return this;
        }

        public Builder setWearTextureParams(int wearTextureParams) {
            this.wearTextureParams = wearTextureParams;
            return this;
        }

        public Builder setWearTextureParams2(int wearTextureParams2) {
            this.wearTextureParams2 = wearTextureParams2;
            return this;
        }

        public Wear build() {
            return new Wear(this);
        }
    }
    
}
