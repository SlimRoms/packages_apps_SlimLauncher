package org.slim.launcher.util;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.R;

import org.slim.launcher.SlimLauncher;

public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {
	private List<ApplicationInfo> appsList = null;
	private Context context;
	private PackageManager packageManager;

	public ApplicationAdapter(Context context, int textViewResourceId,
							  List<ApplicationInfo> appsList) {
		super(context, textViewResourceId, appsList);
		this.context = context;
		this.appsList = appsList;
		packageManager = context.getPackageManager();
	}

	@Override
	public int getCount() {
		return ((null != appsList) ? appsList.size() : 0);
	}

	@Override
	public ApplicationInfo getItem(int position) {
		return ((null != appsList) ? appsList.get(position) : null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (null == view) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(R.layout.snippet_list_row, null);
		}

		ApplicationInfo applicationInfo = appsList.get(position);
		if (null != applicationInfo) {
			TextView appName = (TextView) view.findViewById(R.id.app_name);
			TextView packageName = (TextView) view.findViewById(R.id.app_paackage);
			ImageView iconView = (ImageView) view.findViewById(R.id.app_icon);

			appName.setText(applicationInfo.loadLabel(packageManager));
			packageName.setText(applicationInfo.packageName);

			iconView.setImageDrawable(applicationInfo.loadIcon(packageManager));

			Context context = SlimLauncher.getInstance();
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

			if (sharedPref.getString("hiddenApps", "").contains(applicationInfo.packageName)) {
				view.setBackgroundColor(context.getResources().getColor(R.color.hidden_true));
			} else {
				view.setBackgroundColor(context.getResources().getColor(R.color.hidden_false));
			}
		}
		return view;
	}
};