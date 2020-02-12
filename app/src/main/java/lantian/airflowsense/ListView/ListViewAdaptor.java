package lantian.airflowsense.ListView;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import lantian.airflowsense.FileManager.FileManager;
import lantian.airflowsense.MainActivity;
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
        final SlideBlock block = (SlideBlock)getItem(position);
        View view;
        MyViewHolder viewHolder;

        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new MyViewHolder();
            viewHolder.dateText = view.findViewById(R.id.slide_block_file_date);
            viewHolder.fileNameText = view.findViewById(R.id.slide_block_file_name);
            viewHolder.fileNameText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        String new_name = ((TextView)v).getText().toString();
                        if (FileManager.renameFile(MainActivity.getUserName(), block.getFileName(), block.getPostfix(), new_name)){
                            block.setFileName(new_name);
                            notifyDataSetChanged();
                        }else {
                            ((TextView)v).setText(block.getFileName());
                            notifyDataSetChanged();
                            Toast.makeText(getContext(), "文件名无法更改", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            viewHolder.checkBox = view.findViewById(R.id.slide_block_file_check);
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    block.setCheckState(isChecked);
                }
            });
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
