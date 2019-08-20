package op27no2.parentscope2;

import java.io.File;

public class FileObject {
    private File file;
    private Integer selected;


    public FileObject(File f, Integer s) {
        file = f;
        selected = s;
    }

    public File getFile(){
        return file;
    }
    public void setFile(File f){
        file = f;
    }
    public Integer getSelected(){
        return selected;
    }
    public void setSelected(Integer i){
        selected = i;
    }
}
