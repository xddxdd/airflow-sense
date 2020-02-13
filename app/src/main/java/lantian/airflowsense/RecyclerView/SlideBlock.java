package lantian.airflowsense.RecyclerView;

public class SlideBlock {
    private String date_text;
    private String file_name;
    private String file_name_postfix;
    private boolean checked;

    public SlideBlock(String date, String name, String postfix){
        date_text = date;
        file_name = name;
        file_name_postfix = postfix;
        checked = false;
    }

    public String getDateText(){
        return date_text;
    }

    public String getFileName(){
        return file_name;
    }

    public String getPostfix(){
        return file_name_postfix;
    }

    public void setFileName(String new_name){
        file_name = new_name;
    }

    public boolean isChecked(){
        return checked;
    }

    public void setCheckState(boolean isChecked){
        checked = isChecked;
    }
}
