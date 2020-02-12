package lantian.airflowsense.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import java.util.ArrayList;

import lantian.airflowsense.FileManager.FileManager;
import lantian.airflowsense.MainActivity;
import lantian.airflowsense.R;

public class ListViewManager {

    static private ListViewAdaptor listViewAdaptor;
    static private ArrayList<SlideBlock> dataList;

    static public void ListViewInit(Context context){
        ListViewForScrollView listView = LayoutInflater.from(context).inflate(R.layout.activity_main, null).findViewById(R.id.list_view);
        dataList = new ArrayList<>();
        dataList.add(new SlideBlock("test","test","test"));
        listViewAdaptor = new ListViewAdaptor(context, R.id.slide_block, dataList);
        listView.setAdapter(listViewAdaptor);
    }

    static public void addSlideView(SlideBlock block){
        if (dataList.add(block))
            listViewAdaptor.notifyDataSetChanged();
    }

    static public void removeSelectedSlideView(){
        for (int i = 0; i < dataList.size(); i++){
            if (dataList.get(i).isChecked()) {
                SlideBlock block = dataList.get(i);
                if (FileManager.removeFile(MainActivity.getUserName(), block.getFileName(), block.getPostfix())){
                    dataList.remove(i--);
                }
            }
        }
        listViewAdaptor.notifyDataSetChanged();
    }

}
