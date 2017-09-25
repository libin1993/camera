package cn.cloudworkshop.miaoding.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Author：binge on 2017-05-15 09:41
 * Email：1993911441@qq.com
 * Describe：
 */
public class TailorItemBean implements Serializable {
    private String id;
    private String goods_name;
    private String img_url;
    private String price;
    private String price_type;
    private String spec_ids;
    private String spec_content;
    private List<ItemBean> itemBean;

    private String fabric_id;


    public String getFabric_id() {
        return fabric_id;
    }

    public void setFabric_id(String fabric_id) {
        this.fabric_id = fabric_id;
    }

    public String getSpec_ids() {
        return spec_ids;
    }

    public void setSpec_ids(String spec_ids) {
        this.spec_ids = spec_ids;
    }

    public String getSpec_content() {
        return spec_content;
    }

    public void setSpec_content(String spec_content) {
        this.spec_content = spec_content;
    }


    public String getPrice_type() {
        return price_type;
    }

    public void setPrice_type(String price_type) {
        this.price_type = price_type;
    }

    public String getId() {
        return id;
    }

    public TailorItemBean() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoods_name() {
        return goods_name;
    }

    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }


    public List<ItemBean> getItemBean() {
        return itemBean;
    }

    public void setItemBean(List<ItemBean> itemBean) {
        this.itemBean = itemBean;
    }


    public static class ItemBean implements Serializable {
        private String img;
        private int position_id;


        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public int getPosition_id() {
            return position_id;
        }

        public void setPosition_id(int position_id) {
            this.position_id = position_id;
        }


    }


}
