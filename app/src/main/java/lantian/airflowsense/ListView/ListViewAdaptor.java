package lantian.airflowsense.ListView;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import lantian.airflowsense.R;


public class ListViewAdaptor extends ArrayAdapter {

    private int resourceId;

    public ListViewAdaptor(Context context, int viewId, ArrayList<SlideBlock> list){
        super(context, viewId, list);
        resourceId = viewId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        SlideBlock block = (SlideBlock)getItem(position);
        View view;
        MyViewHolder viewHolder;

        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new MyViewHolder();
            viewHolder.dateText = view.findViewById(R.id.file_date);
            viewHolder.fileNameText = view.findViewById(R.id.file_name);
            viewHolder.checkBox = view.findViewById(R.id.file_check);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (MyViewHolder)view.getTag();
        }
        viewHolder.checkBox.setChecked(block.isChecked());
        viewHolder.fileNameText.setText(block.getFileName());
        viewHolder.dateText.setText(block.getDateText());
        return view;
    }

    class MyViewHolder{
        TextView dateText;
        EditText fileNameText;
        CheckBox checkBox;
    }
}
