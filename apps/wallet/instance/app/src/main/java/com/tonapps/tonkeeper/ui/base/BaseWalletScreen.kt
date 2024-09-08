package com.tonapps.tonkeeper.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.tonapps.extensions.getParcelableCompat
import uikit.base.BaseFragment
import uikit.navigation.Navigation

abstract class BaseWalletScreen<C: ScreenContext>(
    @LayoutRes layoutId: Int,
    screenContext: C,
): BaseFragment(layoutId), BaseWalletVM.Holder {

    private companion object {
        private const val ARG_SCREEN_CONTEXT = "_screen_context"
    }

    override val uiContext: Context
        get() = requireContext()

    abstract val viewModel: BaseWalletVM

    val navigation: Navigation?
        get() = context?.let { Navigation.from(it) }

    val screenContext: C by lazy {
        requireArguments().getParcelable(ARG_SCREEN_CONTEXT)!!
    }

    init {
        putParcelableArg(ARG_SCREEN_CONTEXT, screenContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.attachHolder(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.detachHolder()
    }
}