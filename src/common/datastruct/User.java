package common.datastruct;

import java.net.URL;

import javax.swing.ImageIcon;

import common.GlobalTypeDefine;

public class User {
	private String name;
	private String id;
	private int iconIndex;
	private String address;
	
	public User(String name, String id, int iconIndex) {
		this.name = name;
		this.id = id;
		this.iconIndex = iconIndex;
		this.address = "Unknown";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public int getIconIndex()
    {
        return iconIndex;
    }

    public void setIconIndex(int iconIndex)
    {
        this.iconIndex = iconIndex;
    }

    public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public ImageIcon getSmallIcon() {
	    URL url;
	    if (iconIndex < 0 || iconIndex >= GlobalTypeDefine.USER_HEAD_IMGS.length) {
	        url = this.getClass().getResource("/resource/imgs/small_catwatch.jpg");
	    }
	    else {
	        url = this.getClass().getResource("/resource/imgs/userimgs/small_" + GlobalTypeDefine.USER_HEAD_IMGS[iconIndex]);
	    }
		return new ImageIcon(url); 
	}
	
	   public ImageIcon getIcon() {
	        URL url;
	        if (iconIndex < 0 || iconIndex >= GlobalTypeDefine.USER_HEAD_IMGS.length) {
	            url = this.getClass().getResource("/resource/imgs/catwatch.jpg");
	        } else {
	            url = this.getClass().getResource("/resource/imgs/userimgs/" + GlobalTypeDefine.USER_HEAD_IMGS[iconIndex]);
	        }
	        return new ImageIcon(url); 
	    }

}
