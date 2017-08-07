package day.cloudy.apps.wear.herohelper.sample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.google.android.gms.wearable.Node;

import java.util.List;

class ConnectedNodesAdapter extends RecyclerView.Adapter<ConnectedNodesAdapter.Holder> {

    private final LayoutInflater mInflater;
    private int mSelection;
    private List<Node> mNodes;

    ConnectedNodesAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ConnectedNodesAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(android.R.layout.simple_list_item_single_choice, parent, false);
        return new ConnectedNodesAdapter.Holder(itemView);
    }

    @Override
    public void onBindViewHolder(ConnectedNodesAdapter.Holder holder, int position) {
        holder.text.setText(getItem(position).getDisplayName());
        holder.text.setChecked(mSelection == position);
    }

    @Override
    public int getItemCount() {
        return mNodes != null ? mNodes.size() : 0;
    }

    Node getItem(int position) {
        return mNodes.get(position);
    }

    Node getSelectedItem() {
        return getItem(mSelection);
    }

    void setItems(List<Node> nodes, int selection) {
        mNodes = nodes;
        mSelection = selection;
        notifyDataSetChanged();
    }

    void setSelection(int selection) {
        mSelection = selection;
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {

        private CheckedTextView text;

        Holder(View itemView) {
            super(itemView);
            text = itemView.findViewById(android.R.id.text1);
        }
    }
}
