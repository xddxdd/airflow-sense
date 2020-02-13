package lantian.airflowsense.RecyclerView;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import lantian.airflowsense.FileManager.FileManager;
import lantian.airflowsense.MainActivity;
import lantian.airflowsense.R;


public class RecyclerViewAdaptor extends RecyclerView.Adapter<RecyclerViewAdaptor.MyViewHolder> {

    private Context context;
    private ArrayList<SlideBlock> list;

    public RecyclerViewAdaptor(Context context, ArrayList<SlideBlock> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View inflater = LayoutInflater.from(context).inflate(R.layout.slide_block, parent, false);
        return new MyViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        final SlideBlock block = list.get(position);
        holder.checkBox.setChecked(block.isChecked());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                block.setCheckState(isChecked);
            }
        });
        holder.fileNameText.setText(block.getFileName());
        holder.fileNameText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                        Toast.makeText(context, "文件名无法更改", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        holder.dateText.setText(list.get(position).getDateText());
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView dateText;
        EditText fileNameText;
        CheckBox checkBox;
        public MyViewHolder(View itemView){
            super(itemView);
            dateText = itemView.findViewById(R.id.slide_block_file_date);
            fileNameText = itemView.findViewById(R.id.slide_block_file_name);
            checkBox = itemView.findViewById(R.id.slide_block_file_check);
        }
    }
}
