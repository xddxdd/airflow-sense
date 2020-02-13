package lantian.airflowsense.RecyclerView;

import android.app.Activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import lantian.airflowsense.R;

public class RecyclerViewManager {

    private RecyclerViewAdaptor recyclerViewAdaptor;
    private ArrayList<SlideBlock> dataList;

    public RecyclerViewManager(Activity activity){
        RecyclerView recyclerView = activity.findViewById(R.id.list_view);
        dataList = new ArrayList<>();
        recyclerViewAdaptor = new RecyclerViewAdaptor(activity, dataList);
        LinearLayoutManager manager = new LinearLayoutManager(activity);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(recyclerViewAdaptor);
    }

    public void addSlideView(SlideBlock block){
        dataList.add(block);
    }

    public ArrayList<SlideBlock> getCheckedList(){
        ArrayList<SlideBlock> checkedList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++){
            if (dataList.get(i).isChecked()) {
                checkedList.add(dataList.get(i));
            }
        }
        return checkedList;
    }

    public void removeSlideBlock(SlideBlock block){
        dataList.remove(block);
    }

    public void updateView(){
        recyclerViewAdaptor.notifyDataSetChanged();
    }

    public void clearView(){
        dataList.clear();
        recyclerViewAdaptor.notifyDataSetChanged();
    }

}
