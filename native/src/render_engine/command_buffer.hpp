/*!
 * \author ddubois 
 * \date 01-Sep-18.
 */

#ifndef NOVA_RENDERER_COMMAND_BUFFER_HPP
#define NOVA_RENDERER_COMMAND_BUFFER_HPP

#include <functional>
#include "../util/smart_enum.hpp"

namespace nova {
    /*!
     * \brief The type of command list we want to create. This is important for DX12 but I don't think it's important
     * for Vulkan - but it needs to be part of the DX12 interface, so here we are
     */
    SMART_ENUM(command_buffer_type,
        /*!
         * \brief Command buffer will be directly executed by the GPU. Usually used for rendering things
         */
        DIRECT,

        /*!
         * \brief Command buffer will be used for copying data from one buffer to another (e.g. from a staging buffer
         * to a texture)
         */
        COPY,

        /*!
         * \brief Command buffer will be used for compute tasks
         */
        COMPUTE
    );

    /*!
     * \brief Platform-independent interface for a command buffer
     */
    class command_buffer {
        /*!
         * \brief Executes a provided function when this command buffer has finished executing
         * \param completion_handler The function to execute when this command buffer has finished executing
         */
        virtual void on_completion(std::function<void(void)> completion_handler) = 0;
    };
}

#endif //NOVA_RENDERER_COMMAND_BUFFER_HPP