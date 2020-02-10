package lantian.airflowsense.ListView;

public class SlideBlock {
    private String date_text;
    private String file_name;
    private boolean checked;

    public SlideBlock(String date, String name){
        date_text = date;
        file_name = name;
        checked = false;
    }

    public String getDateText(){
        return date_text;
    }

    public String getFileName(){
        return file_name;
    }

    public void setFileName(String new_name){
        file_name = new_name;
    }

    public boolean isChecked(){
        return checked;
    }

    public void reverseCheckState(){
        checked = !checked;
    }
}
